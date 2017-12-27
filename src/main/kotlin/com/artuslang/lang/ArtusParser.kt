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

import com.artuslang.core.scopes.ArtusBasicScope
import com.artuslang.core.ArtusBitArray
import com.artuslang.core.scopes.ArtusScope
import com.artuslang.lang.matching.LexerToken
import java.io.File

/**
 * Created on 12/12/2017 by Frederic
 */
class ArtusParser {

    val globalScope = ArtusBasicScope(GlobalLogger())

    val lexedFiles = HashMap<File, List<LexerToken>>()

    fun parseFile(file: File, contextType: ArtusContextType = LexerDefaults.defaultContextType) {
        lexedFiles.put(file, ArtusLexer(this, globalScope, file.canonicalPath, file.readText(), contextType).lex())
    }

    private val compileChain = ArrayList<CompileEvent>()

    fun addFileCompiler(path: String, scope: ArtusScope) {
        compileChain.add(FileCompiler(path, scope))
    }

    interface CompileEvent { fun onCompile(options: List<String>) }
    inner class FileCompiler(path: String, val scope: ArtusScope): CompileEvent {
        val file = File(path)
        override fun onCompile(options: List<String>) {
            file.writeBytes(scope.compile(ArtusBitArray()).toByteArray())
        }
    }

    fun compile(vararg options: String) {
        compileChain.forEach { it.onCompile(options.toList()) }
    }
}