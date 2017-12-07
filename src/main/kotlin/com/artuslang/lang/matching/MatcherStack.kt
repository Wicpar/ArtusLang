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
import java.util.*

/**
 * Created on 06/12/2017 by Frederic
 */
class MatcherStack(val name: String, list: Collection<OrderedMatcher> = listOf()) {

    constructor(name: String, list: List<Matcher>): this(name, list.mapIndexed { index, matcher -> OrderedMatcher(index, matcher) } as Collection<OrderedMatcher>)

    private val lst = PriorityQueue<OrderedMatcher>(list)

    fun addMatcher(matcher: OrderedMatcher) {
        lst.add(matcher)
    }

    fun addMatchers(list: Collection<OrderedMatcher>) {
        lst.addAll(list)
    }

    class OrderedMatcher(private val ordinal: Int, val matcher: Matcher):  Comparable<OrderedMatcher> {

        override fun compareTo(other: OrderedMatcher): Int {
            return ordinal.compareTo(other.ordinal)
        }
    }

    fun findNext(lexer: ArtusLexer): LexerToken? {
        return lst.fold(null as LexerToken?, { acc, it -> acc ?: it.matcher.find(lexer) })
    }

    override fun toString(): String {
        return lst.joinToString(", ") { it.matcher.type.name }
    }
}