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

package com.artuslang.lang

import com.artuslang.new.*

/**
 * Created on 23/01/2018 by Frederic
 */

class BaseLang() {

    val baseCtx = ScriptContext()

    val utils = LangUtils(baseCtx)

    val noType = TokenType("", hashMapOf())
    val sscriptTagType = TokenType("scriptTag", hashMapOf())

    val spaceMatcher = TokenMatcher(noType, "[\\p{Z}\\r]+|\\n")
    val spaceContext = utils.contextMatcherNop(spaceMatcher)
    val spaceableContext = ContextType("spaceable", arrayListOf(spaceContext))


    val directiveMatcher = ContextMatcher(TokenMatcher(sscriptTagType, "#.*?\\n"), { token, context ->
        println(token.text.substring(1, token.text.length - 1))
        utils.eval(token.text.substring(1, token.text.length - 1), Pair("token", token), Pair("context", context)) as? Context
                ?: context
    })

    val multilineMatcher = ContextMatcher(TokenMatcher(sscriptTagType, "###.*?###"), { token, context ->
        println(token.text.substring(3, token.text.length - 3))
        utils.eval(token.text.substring(3, token.text.length - 3), Pair("token", token), Pair("context", context)) as? Context
                ?: context
    })

    val baseContextType = ContextType("script", arrayListOf(multilineMatcher, directiveMatcher), arrayListOf(spaceableContext))
}


fun main(args: Array<String>) {
    val script =
            """
                # log:println("yolo");


                ###
                    log:println(context.tokens);
                    var basectx = context.type;
                    log:println(basectx.name);
                    var ln = function(a) {
                        log:println(a);
                    };
                    this:set("println", ln);
                    println("lol");
                ###
            """.trimIndent()
    val lang = BaseLang()
    StringArtusReader(script, "script").build(Context(lang.baseContextType))
}