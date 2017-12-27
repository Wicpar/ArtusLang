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

package com.artuslang.core.scopes

import com.artuslang.core.scopes.handlers.ArtusHierachicalComponentHandler
import com.artuslang.lang.ContextualizedLogger

open class ArtusHierarchicalScope(origin: ContextualizedLogger, val parents: List<ArtusScope> = listOf()) : ArtusBasicScope(origin) {
    constructor(origin: ContextualizedLogger, vararg parents: ArtusScope): this(origin, parents.asList())
    override val components: ArtusHierachicalComponentHandler = ArtusHierachicalComponentHandler(parents.map {
        val base = if (it is ArtusHierarchicalScope)
            it.components.parents
        else
            listOf()
        base + it.components
    }.fold(listOf(), { acc, list -> (acc + list).distinct() }))
}