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
import java.util.logging.Level

class ContextualizedLogger(val lexer: ArtusLexer, val token: LexerToken) {
    fun log(level: String, obj: Any?) {
        println("${lexer.origin}:${lexer.getFilePosRange(token.textRange)}: ${Level.parse(level.toUpperCase()).name}: ${obj?.toString()}")
    }
}