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

package com.artuslang

import org.junit.jupiter.api.Test
import kotlin.system.measureNanoTime

/**
 * Created on 23/01/2018 by Frederic
 */

class BaseLang {
    val baseCtx = ScriptContext()
    val utils = LangUtils(baseCtx)
    val noType = TokenType("", hashMapOf())
    val sscriptTagType = TokenType("directive", hashMapOf())

    val spaceMatcher = TokenMatcher(noType, "[\\p{Z}\\r]+|\\n")
    val spaceContext = utils.contextMatcherNop(spaceMatcher)
    val spaceableContext = ContextType("spaceable", arrayListOf(spaceContext))

    val directiveMatcher = ContextMatcher(TokenMatcher(sscriptTagType, "#.*?(?:\\n|\\z)"), { token, context ->
        utils.eval(token.text.substring(1, token.text.length - 1), Pair("context", context)) as? Context
                ?: context
    })

    val multilineMatcher = ContextMatcher(TokenMatcher(sscriptTagType, "###.*?###"), { token, context ->
        utils.eval(token.text.substring(3, token.text.length - 3), Pair("context", context)) as? Context
                ?: context
    })

    val baseContextType = ContextType("base", arrayListOf(multilineMatcher, directiveMatcher), arrayListOf(spaceableContext))

    init {
        val contexts = HashMap(listOf(spaceableContext, baseContextType).associate { Pair(it.name, it) })
        val matchers = hashMapOf(Pair("spaces", spaceMatcher))
        val tokens = HashMap(listOf(noType, sscriptTagType).associate { Pair(it.name, it) })
        baseCtx.registerNamespace("contexts", contexts)
        baseCtx.registerNamespace("matchers", matchers)
        baseCtx.registerNamespace("tokens", tokens)
    }
}

internal object Test {

    @Test
    fun test() {
        main(arrayOf())
    }
}

fun main(args: Array<String>) {
    //preheat jexl
    BaseLang().apply { StringArtusReader("###;###", "").build(Context(baseContextType)) }
    //real thing
    println("${measureNanoTime {
        BaseLang().apply { utils.include("src/test/kotlin/com/artuslang/testfiles/main.artus", Context(baseContextType)) }
    } / 1000000f} ms")
}
