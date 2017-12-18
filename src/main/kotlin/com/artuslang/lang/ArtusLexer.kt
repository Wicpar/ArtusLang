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
import com.artuslang.lang.matching.TokenType
import com.artuslang.lang.util.FilePos
import com.artuslang.lang.util.FilePosRange
import java.io.File
import java.util.*
import kotlin.collections.HashSet

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

    private

    fun hasNext(): Boolean {
        return sequence.isNotEmpty()
    }

    fun findNext(): LexerToken {
        return context.type.findNext(this)
    }

    private val lst = arrayListOf<LexerToken>()
    private val tracker = Stack<Int>()
    fun lex(): List<LexerToken> {
        tracker.clear()
        tracker.push(0)
        lst.clear()
        while(hasNext())
            lst.add(findNext())
        return lst
    }

    /**
     * insert tracker at current index
     */
    fun pushTracker() {
        tracker.push(lst.size)
    }

    /**
     * remove tracker
     */
    fun popTracker() {
        tracker.pop()
    }

    /**
     * get tracked list, up until current index, excluded
     */
    fun getTrackedList(): List<LexerToken> {
        return lst.subList(tracker.peek(), lst.size)
    }

    /**
     * get tracked list, up until current index, excluded
     * filtered by type name
     */
    fun getTrackedListFiltered(filter: Array<String>): List<LexerToken> {
        val hash = HashSet(filter.toList())
        return getTrackedList().filter { hash.contains(it.type.name) }
    }

    /**
     * get tracked list, up until current index, excluded
     * filtered by type
     */
    fun getTrackedListFiltered(filter: Array<TokenType>): List<LexerToken> {
        val hash = HashSet(filter.toList())
        return getTrackedList().filter { hash.contains(it.type) }
    }
}

