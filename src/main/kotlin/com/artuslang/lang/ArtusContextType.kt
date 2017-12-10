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

import com.artuslang.lang.matching.LexerToken
import com.artuslang.lang.matching.Matcher
import com.artuslang.lang.matching.TokenType
import org.apache.commons.jexl3.JexlContext
import org.apache.commons.jexl3.internal.Closure

class ArtusContextType(val name: String, val matcherStack: Array<Matcher>, actions: Map<TokenType, Any?>) {

    val actions: Map<TokenType, ContextAction> = actions.mapNotNull {
        val v = it.value ?: return@mapNotNull null
        Pair(it.key, when (v) {
            is String -> object: ContextAction {
                val script = JEXLConfiguration.jexl.createScript(v)
                override fun run(ctx: JexlContext) {
                    script.execute(ctx)
                }
            }
            is Closure -> object: ContextAction {
                override fun run(ctx: JexlContext) {
                    v.execute(ctx)
                }
            }
            is ContextAction -> v
            else -> throw Exception("${v.javaClass.name} object not supported as action")
        })
    }.associate { it }


    fun findNext(lexer: ArtusLexer): LexerToken {
        val token = matcherStack.fold(null as LexerToken?, { acc, it -> acc ?: it.find(lexer) }) ?: throw ArtusLexerException("${lexer.origin}: no token matched at index ${lexer.index}")
        val ctx = lexer.context
        ctx.token = token
        actions[token.type]?.run(ctx.jexl)
        return token
    }

    fun matcherStackString(): String {
        return "(${matcherStack.joinToString(", ") {"<${it.type.name}>"}})"
    }

    interface ContextAction {
        fun run(ctx: JexlContext)
    }
}