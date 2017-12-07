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

import com.artuslang.lang.ArtusLexer
import org.intellij.lang.annotations.Language

/**
 * Created on 06/12/2017 by Frederic
 */
open class Matcher(val type: TokenType, @Language("RegExp") pattern: String, val group: Int = 1) {
    val regex = Regex("^($pattern).*", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.UNIX_LINES))
    fun find(lexer: ArtusLexer): LexerToken? {
        val res = regex.find(lexer.sequence, 0) ?: return null
        val group = res.groups[group] ?: return null
        val ret = LexerToken(type, group.value, group.range.let { IntRange(it.start + lexer.index, it.endInclusive + lexer.index) })
        lexer.index += ret.text.length
        lexer.sequence = lexer.sequence.subSequence(ret.text.length, lexer.sequence.length)
        return ret
    }
}