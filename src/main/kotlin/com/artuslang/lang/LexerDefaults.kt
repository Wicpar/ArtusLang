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

object LexerDefaults {
    val error = TokenType("error")
    val scriptLimit = TokenType("scriptLimit")
    val scriptContent = TokenType("scriptContent")
    val defaultTokenMap = listOf(error, scriptContent, scriptLimit).associate { Pair(it.name, it) }
    val errorMatcher = Matcher(error, ".")
    val headerLimitMatcher = Matcher(scriptLimit, "\"\"\"")
    val headerContentMatcher = Matcher(scriptContent, "(.*?)\"\"\"", 2)
    val defaultMatcherMap = listOf(errorMatcher, headerLimitMatcher, headerContentMatcher).associate { Pair(it.type.name, it) }
    val defaultMatcherStack = arrayListOf(headerLimitMatcher, errorMatcher)
    val scriptMatcherStack = arrayListOf(headerLimitMatcher, headerContentMatcher, errorMatcher)
    val defaultContextType = ArtusContextType("default", null, defaultMatcherStack, mapOf(Pair(scriptLimit, "lexer.pushContext(repo.getContextType('script'))")))
    val scriptContextType = ArtusContextType("script", null, scriptMatcherStack, mapOf(Pair(scriptLimit, "lexer.popContext()"), Pair(scriptContent, "eval(token.text)")))
    val contextMap = mapOf(Pair("default", defaultContextType), Pair("script", scriptContextType))
}