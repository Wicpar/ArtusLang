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

import com.artuslang.new.ContextType
import com.artuslang.new.TokenMatcher
import com.artuslang.new.TokenType

/**
 * Created on 23/01/2018 by Frederic
 */

val noType = TokenType("", hashMapOf())
val sscriptTagType = TokenType("scriptTag", hashMapOf())

val spaceMatcher = TokenMatcher(noType, "[\\p{Z}\\r]+|\\n")
val spaceContext = spaceMatcher.withContext { _, context ->  context}
val spaceableContext = ContextType("spaceable", arrayListOf(spaceContext))

val lineMatcher = TokenMatcher(noType, ".*(?!\n)")

val directiveMatcher = TokenMatcher(sscriptTagType, "#").withContext { _, context ->
    context
}
val multilineMatcher = TokenMatcher(sscriptTagType, "###").withContext { _, context ->
    context
}

val baseContextType = ContextType("script", arrayListOf(multilineMatcher, directiveMatcher), arrayListOf(spaceableContext))

fun main(args: Array<String>) {
//    val g = allMatcher.regex.find("aaabbbaaaa")
//    println(g?.range)
//    println(g?.value)
}