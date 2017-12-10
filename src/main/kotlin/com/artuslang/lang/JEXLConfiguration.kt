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
import com.artuslang.lang.matching.TokenType
import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.introspection.JexlSandbox
import java.util.regex.Pattern

/**
 * Created on 06/12/2017 by Frederic
 */
object JEXLConfiguration {
    val jexl = JexlBuilder().sandbox({
        val sandbox = JexlSandbox(false)
        sandbox.white(String::class.java.name)
        sandbox.white(ArtusLexer::class.java.name)
        sandbox.white(GlobalRepo::class.java.name)
        sandbox.white(Matcher::class.java.name)
        sandbox.white(ArtusContext::class.java.name)
        sandbox.white(ArtusContextType::class.java.name)
        sandbox.white(TokenType::class.java.name)
        sandbox.white(Map::class.java.name)
        sandbox.white(List::class.java.name)
        sandbox.white(HashMap::class.java.name)
        sandbox.white(ArrayList::class.java.name)
        sandbox.white(ArtusScope::class.java.name)
        sandbox.white(LexerToken::class.java.name)
        sandbox.white(Pattern::class.java.name)
        sandbox
    }()).create()
}