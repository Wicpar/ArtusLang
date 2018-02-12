/*
 * Copyright 2018 - present Frederic Artus Nieto
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

package com.artuslang.new

import org.intellij.lang.annotations.Language

/**
 * Created on 22/01/2018 by Frederic
 */


class ArtusLocation(val origin: String, val range: IntRange)

class TokenType(val name: String, val properties: HashMap<String, Any>)

class Token(val location: ArtusLocation, val text: String, val type: TokenType)

class TokenMatcher(val type: TokenType, @Language("RegExp") val pattern: String, val group: Int = 1) {
    val regex = Regex("^($pattern)", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL, RegexOption.UNIX_LINES))

    fun withContext(event: (Token, Context) -> Context): ContextMatcher {
        return ContextMatcher(this, event)
    }
}

class ContextMatcher(val matcher: TokenMatcher, val event: (Token, Context) -> Context)

class ContextType(val name: String, val matchers: ArrayList<ContextMatcher>, val parents: ArrayList<ContextType> = arrayListOf()) {
    val stack: List<ContextMatcher>
        get() = matchers + parents.map { stack }.flatten()
}

class Context(val type: ContextType, val parent: Context? = null) {
    val children = HashMap<ContextType, ArrayList<Context>>()
    val properties = HashMap<String, Any>()
    val tokens = ArrayList<Token>()
    fun pushToken(token: Token) {
        tokens.add(token)
        parent?.pushToken(token)
    }

    fun child(contextType: ContextType): Context {
        val ctx = Context(contextType, this)
        children.getOrPut(contextType, { arrayListOf() }).add(ctx)
        return ctx
    }
}

interface ArtusReader {
    val name: String
    fun build(ctx: Context): Context
}

class StringArtusReader(val str: String, override val name: String): ArtusReader {
    private var offset = 0

    override fun build(ctx: Context): Context {
        return ctx.type.stack.fold(null as Context?, { acc, contextMatcher ->
            acc ?: {
                val matched = contextMatcher.matcher.regex.find(str, offset)
                matched?.groups?.get(contextMatcher.matcher.group)?.let {
                    offset = it.range.endInclusive + 1
                    val token = Token(ArtusLocation(name, IntRange(it.range.start, it.range.endInclusive)), it.value, contextMatcher.matcher.type)
                    contextMatcher.event(token, ctx)
                }
            }()
        }) ?: throw RuntimeException("no Context in source $name at $offset: \"${str.subSequence(offset, offset + 20)}...\"")
    }

}
