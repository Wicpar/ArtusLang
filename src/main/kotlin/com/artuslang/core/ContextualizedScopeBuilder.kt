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

package com.artuslang.core

import com.artuslang.core.scopes.ArtusScope
import org.apache.commons.jexl3.JexlContext
import org.apache.commons.jexl3.internal.Closure

class ContextualizedScopeBuilder(val builder: (Map<Any, ArtusScope>, ArtusScope) -> ArtusScope, val filter: (Map<Any, ArtusScope>) -> Boolean, val aliases: Map<Any, Any>, val scope: ArtusScope, val index: Int): Comparable<ContextualizedScopeBuilder> {

    constructor(builder: Closure, filter: Closure, aliases: Map<Any, Any>, scope: ArtusScope, index: Int, jexl: JexlContext): this({ map, scop -> builder.execute(jexl, map, scop) as ArtusScope }, { map -> filter.execute(jexl, map) as Boolean}, aliases, scope, index)

    override fun compareTo(other: ContextualizedScopeBuilder): Int {
        return index.compareTo(other.index)
    }
}