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
import org.apache.commons.jexl3.internal.Closure


class ArtusContextType @JvmOverloads constructor(val name: String, val entryMatcher: Matcher?, val matcherStack: ArrayList<Matcher>, actions: Map<TokenType, Any?>, val parent: ArtusContextType? = null) {

    val actions: HashMap<TokenType, ContextAction> = HashMap(actions.mapNotNull {
        val v = it.value ?: return@mapNotNull null
        Pair(it.key, when (v) {
            is String -> object : ContextAction {
                val script = JEXLConfiguration.jexl.createScript(v)
                override fun run(ctx: ArtusContext) {
                    script.execute(ctx.jexl)
                }
            }
            is Closure -> object : ContextAction {
                override fun run(ctx: ArtusContext) {
                    v.execute(ctx.jexl)
                }
            }
            is ContextAction -> v
            else -> throw Exception("${v.javaClass.name} object not supported as action")
        })
    }.associate { it })

    @JvmOverloads
    fun addMatchAction(matcher: Matcher, v: Any?, front: Boolean = true): ContextAction? {
        if (front)
            matcherStack.add(0, matcher)
        else
            matcherStack.add(matcher)
        if (v == null) return actions.remove(matcher.type)
        return actions.put(matcher.type, when (v) {
            is String -> object : ContextAction {
                val script = JEXLConfiguration.jexl.createScript(v)
                override fun run(ctx: ArtusContext) {
                    script.execute(ctx.jexl)
                }
            }
            is Closure -> object : ContextAction {
                override fun run(ctx: ArtusContext) {
                    v.execute(ctx.jexl)
                }
            }
            is ContextAction -> v
            else -> throw Exception("${v.javaClass.name} object not supported as action")
        })
    }

    @JvmOverloads
    fun addContextPush(context: ArtusContextType, matcher: Matcher? = null): ContextAction? {
        val matcher1 = matcher ?: context.entryMatcher ?: throw Exception("${context.name} cannot be added as context push, it does not have an entry matcher.")
        return addMatchAction(matcher1, object : ContextAction {
            override fun run(ctx: ArtusContext) {
                ctx.lexer.pushContext(context)
            }
        })
    }

    @JvmOverloads
    fun addContextChange(context: ArtusContextType, matcher: Matcher? = null): ContextAction? {
        val matcher1 = matcher ?: context.entryMatcher ?: throw Exception("${context.name} cannot be added as context change, it does not have an entry matcher.")
        return addMatchAction(matcher1, object : ContextAction {
            override fun run(ctx: ArtusContext) {
                ctx.lexer.changeContext(context)
            }
        })
    }

    fun addContextPop(matcher: Matcher): ContextAction? {
        return addMatchAction(matcher, ContextAction.popCtx)
    }

    fun findNext(lexer: ArtusLexer): LexerToken {
        val token = findToken(lexer) ?: throw ArtusLexerException("${lexer.origin}: no token matched at index ${lexer.index}")
        val ctx = lexer.context
        ctx.token = token
        try {
            getAction(token.type)?.run(ctx)
        } catch (e: Exception) {
            ctx.log("severe", e.message)
            throw e
        }
        return token
    }

    private fun findToken(lexer: ArtusLexer): LexerToken? {
        return matcherStack.fold(null as LexerToken?, { acc, it -> acc ?: it.find(lexer) }) ?: parent?.findToken(lexer)
    }

    private fun getAction(type: TokenType): ContextAction? {
        return actions[type] ?: parent?.getAction(type)
    }

    fun matcherStackString(): String {
        return "(${matcherStack.joinToString(", ") { "<${it.type.name}>" }})"
    }

    interface ContextAction {
        companion object {
            val popCtx = object : ContextAction {
                override fun run(ctx: ArtusContext) {
                    ctx.lexer.popContext()
                }
            }
        }
        fun run(ctx: ArtusContext)
    }
}