/*
 * Copyright 2017 - present Frederic Artus Nieto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.artuslang.lang

import com.artuslang.core.ArtusScope
import com.artuslang.lang.matching.LexerToken
import com.artuslang.lang.matching.Matcher
import com.artuslang.lang.matching.MatcherStack
import com.artuslang.lang.matching.TokenType
import org.apache.commons.jexl3.JexlContext
import org.apache.commons.jexl3.ObjectContext
import java.util.*
import kotlin.collections.HashMap

/**
 * Created on 06/12/2017 by Frederic
 */
class ArtusLexer(val globalScope: ArtusScope, val origin: String, charSequence: CharSequence) {
    private val contextStack = Stack<ArtusContext>()
    val context: ArtusContext
        @JvmName("getContext")
        get() = contextStack.peek()
    fun popContext() {
        contextStack.pop()
    }

    @JvmOverloads
    fun pushContext(ctxType: ArtusContextType, artusScope: ArtusScope = context.scope) {
        contextStack.push(ArtusContext(ctxType, this, artusScope))
    }
    init {
        pushContext(LexerDefaults.defaultContextType, globalScope)
    }

    var index = 0
    var sequence = charSequence
    fun hasNext(): Boolean {
        return sequence.isNotEmpty()
    }
    fun findNext(): LexerToken {
        return context.type.findNext(this)
    }
    fun findAll(): List<LexerToken> {
        val lst = arrayListOf<LexerToken>()
        while(hasNext())
            lst.add(findNext())
        return lst
    }
}

object LexerDefaults {
    val error = TokenType("error")
    val scriptLimit = TokenType("scriptLimit")
    val scriptContent = TokenType("scriptContent")
    val defaultTokenMap = listOf(error, scriptContent, scriptLimit).associate { Pair(it.name, it) }
    val errorMatcher = Matcher(error, ".")
    val headerLimitMatcher = Matcher(scriptLimit, "\"\"\"")
    val headerContentMatcher = Matcher(scriptContent, "(.*?)\"\"\"", 2)
    val defaultMatcherMap = listOf(errorMatcher, headerLimitMatcher, headerContentMatcher).associate { Pair(it.type.name, it) }
    val defaultMatcherStack = MatcherStack(listOf(headerLimitMatcher, errorMatcher))
    val scriptMatcherStack = MatcherStack(listOf(headerLimitMatcher, headerContentMatcher, errorMatcher))
    val matcherStackMap = mapOf(Pair("default", defaultMatcherStack), Pair("script", scriptMatcherStack))
    val defaultContextType = ArtusContextType("default", defaultMatcherStack, mapOf(Pair(scriptLimit, "lexer.pushContext(repo.contextTypes.get('script'))")))
    val scriptContextType = ArtusContextType("default", scriptMatcherStack, mapOf(Pair(scriptLimit, "lexer.popContext()"), Pair(scriptContent, "eval(token.text)")))
    val contextMap = mapOf(Pair("default", defaultContextType), Pair("script", scriptContextType))
}

object GlobalRepo {
    val tokens: HashMap<String, TokenType> = HashMap(LexerDefaults.defaultTokenMap)
    val matchers: HashMap<String, Matcher> = HashMap(LexerDefaults.defaultMatcherMap)
    val matcherStacks: HashMap<String, MatcherStack> = HashMap(LexerDefaults.matcherStackMap)
    val contextTypes: HashMap<String, ArtusContextType> = HashMap(LexerDefaults.contextMap)
}

class ArtusContext(val type: ArtusContextType, val lexer: ArtusLexer, val scope: ArtusScope) {
    /**
     * only for actions
     */
    lateinit var token: LexerToken
    val repo = GlobalRepo
    val jexl: JexlContext = ObjectContext(JEXLConfiguration.jexl, this)
    fun eval(str: String): Any? {
        return JEXLConfiguration.jexl.createScript(str).execute(jexl)
    }
}

class ArtusContextType(val name: String, private val matcherStack: MatcherStack, actions: Map<TokenType, String>) {

    private val actions: Map<TokenType, ContextAction> = actions.map { Pair(it.key, object: ContextAction {
        val script = JEXLConfiguration.jexl.createScript(it.value)
        override fun run(ctx: JexlContext) {
            script.execute(ctx)
        }
    }) }.associate { it }

    fun findNext(lexer: ArtusLexer): LexerToken {
        val token = matcherStack.findNext(lexer) ?: throw ArtusLexerException("${lexer.origin}: no token matched at index ${lexer.index}")
        lexer.context.token = token
        actions[token.type]?.run(lexer.context.jexl)
        return token
    }

    interface ContextAction {
        fun run(ctx: JexlContext)
    }
}

class ArtusLexerException(s: String) : Exception(s)
