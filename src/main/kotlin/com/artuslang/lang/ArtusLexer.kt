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
import com.artuslang.lang.util.FilePos
import com.artuslang.lang.util.FilePosRange
import java.io.File
import java.util.*

/**
 * Created on 06/12/2017 by Frederic
 */
class ArtusLexer(val parser: ArtusParser, val globalScope: ArtusScope, val origin: String, charSequence: CharSequence, contextType: ArtusContextType = LexerDefaults.defaultContextType) {

    @JvmOverloads
    fun includeFromPath(string: String, contextType: ArtusContextType = LexerDefaults.defaultContextType) {
        parser.parseFile(File(string), contextType)
    }

    private val contextStack = Stack<ArtusContext>()
    val context: ArtusContext
        @JvmName("getContext")
        get() = contextStack.peek()
    fun popContext() {
        contextStack.pop()
    }

    @JvmOverloads
    fun pushContext(ctxType: ArtusContextType, artusScope: ArtusScope = context.scope) {
        contextStack.push(ArtusContext(ctxType, this, artusScope))
    }

    @JvmOverloads
    fun changeContext(ctxType: ArtusContextType, artusScope: ArtusScope = context.scope) {
        contextStack.pop()
        contextStack.push(ArtusContext(ctxType, this, artusScope))
    }

    init {
        pushContext(contextType, globalScope)
    }

    private val linesIndexes = charSequence.mapIndexed { index, c -> Pair(index, c) }.filter { it.second == '\n' }.map { it.first }
    fun getFilePos(pos: Int): FilePos {
        val line = linesIndexes.indexOfFirst { it > pos }.let { if (it == -1) 0 else it }
        val linePos = if (line == 0) 0 else linesIndexes[line - 1]
        val offset = pos - linePos
        return FilePos(line, offset)
    }
    fun getFilePosRange(pos: IntRange): FilePosRange {
        return FilePosRange(getFilePos(pos.start), getFilePos(pos.endInclusive))
    }

    var index = 0
    var sequence = charSequence

    fun hasNext(): Boolean {
        return sequence.isNotEmpty()
    }

    fun findNext(): LexerToken {
        return context.type.findNext(this)
    }

    fun findAll(): List<LexerToken> {
        val lst = arrayListOf<LexerToken>()
        while(hasNext())
            lst.add(findNext())
        return lst
    }
}

