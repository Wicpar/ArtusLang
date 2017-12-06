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

package com.artuslang.lang.matching

import com.artuslang.core.ArtusScope
import com.artuslang.lang.ArtusLexer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Created on 06/12/2017 by Frederic
 */
internal class MatcherTest {

    @Test
    fun find() {
        val matcher = Matcher(TokenType(""), "abcd")
        val lexer = ArtusLexer(ArtusScope(), "", "abcdefgh")
        lexer.index = 1
        val res = matcher.find(lexer)
        assertEquals("abcd", res?.text)
        assertEquals(1, res?.textRange?.start)
        assertEquals(4, res?.textRange?.endInclusive)
    }

}