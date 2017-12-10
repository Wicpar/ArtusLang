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

import com.artuslang.lang.matching.Matcher
import com.artuslang.lang.matching.TokenType
import org.intellij.lang.annotations.Language

object GlobalRepo {
    private val modules = hashSetOf<Any>()
    fun hasModule(elem: Any): Boolean {
        return modules.contains(elem)
    }
    fun registerModule(elem: Any) {
        modules.add(elem)
    }

    private val tokens: HashMap<String, TokenType> = HashMap(LexerDefaults.defaultTokenMap)

    fun tokenTypeOf(str: String): TokenType {
        return tokens.getOrPut(str, {TokenType(str)})
    }

    private val matchers: HashMap<String, Matcher> = HashMap(LexerDefaults.defaultMatcherMap)

    @JvmOverloads
    fun registerMatcher(str: String, @Language("RegExp") pattern: String, group: Int = 1): Matcher {
        val ret = Matcher(tokenTypeOf(str), pattern, group)
        matchers.put(str, ret)
        return ret
    }

    fun getMatcher(str: String): Matcher? {
        return matchers[str]
    }

    private val contextTypes: HashMap<String, ArtusContextType> = HashMap(LexerDefaults.contextMap)

    @JvmOverloads
    fun extendContextType(str: String, ctx: ArtusContextType, list: Array<Matcher>, actions: Map<TokenType, Any?> = mapOf()): ArtusContextType {
        return registerContextType("${ctx.name}.$str", list + ctx.matcherStack, actions + ctx.actions)
    }

    fun registerContextType(str: String, stack: Array<Matcher>, actions: Map<TokenType, Any?>): ArtusContextType {
        val ret = ArtusContextType(str, stack, actions)
        contextTypes.put(str, ret)
        return ret
    }

    fun getContextType(str: String): ArtusContextType? {
        return contextTypes[str]
    }

    fun getContextType(ctx: ArtusContextType, str: String): ArtusContextType? {
        return contextTypes[ctx.name + "." + str]
    }

    private val utils = HashMap<Any, Any?>()
    fun getUtil(obj: Any): Any? {
        return utils[obj]
    }
    fun setUtil(obj: Any, y: Any?): Any? {
        return utils.put(obj, y)
    }
}