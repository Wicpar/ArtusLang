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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream


/**
 * Created on 06/12/2017 by Frederic
 */
internal class ArtusLexerTest {
    private val outContent = ByteArrayOutputStream()
    private val errContent = ByteArrayOutputStream()

    @BeforeEach
    fun setUpStreams() {
        System.setOut(PrintStream(outContent))
        System.setErr(PrintStream(errContent))
    }

    @AfterEach
    fun cleanUpStreams() {
        System.setOut(null)
        System.setErr(null)
    }

    @Test
    fun testExec() {
        val lexer = ArtusLexer(ArtusScope(), "", "\"\"\"\n\"\"\"")
        assertEquals(
                listOf(LexerDefaults.scriptLimit.name, LexerDefaults.scriptContent.name, LexerDefaults.scriptLimit.name),
                lexer.findAll().map { it.type.name })
    }

    @Test
    fun testLogger() {
        val lexer = ArtusLexer(ArtusScope(), "", "\"\"\"log('info', 'hello world')\"\"\"")
        assertEquals(
                listOf(LexerDefaults.scriptLimit.name, LexerDefaults.scriptContent.name, LexerDefaults.scriptLimit.name),
                lexer.findAll().map { it.type.name })
        assertEquals(outContent.toString().trim(), "(0:3):(0:28): INFO: hello world")
    }



}