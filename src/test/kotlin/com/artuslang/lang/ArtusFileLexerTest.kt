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
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Created on 07/12/2017 by Frederic
 */
internal class ArtusFileLexerTest {

    @Test
    fun testFile() {
        val file = File("src/test/kotlin/com/artuslang/testSimpleLexer.artus")
        val lexer = ArtusLexer(ArtusScope(), "", file.readText())
        val time = System.nanoTime()
        val lst = lexer.findAll()
        println(System.nanoTime() - time)
        fun printHierarchy(scope: ArtusScope, step: Int = 0) {
            scope.components.forEach {
                println(it.key.padStart(step * 4))
                printHierarchy(it.value, step + 1)
            }
        }
        printHierarchy(lexer.globalScope, 1)
    }
}