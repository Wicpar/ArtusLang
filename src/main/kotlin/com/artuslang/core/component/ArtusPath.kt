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

package com.artuslang.core.component

import com.artuslang.core.ArtusPathException
import com.artuslang.core.ArtusScope

class ArtusPath(val relative: Boolean, val succession: List<ArtusId>) {

    fun resolve(thisScope: ArtusScope, rootScope: ArtusScope): ArtusScope {
        val scope = if (relative) thisScope else rootScope
        return succession.foldIndexed(scope, {
            idx: Int, acc: ArtusScope, elem: ArtusId ->
            acc.components
                    .filter { it.isAvailableFor(elem.base) }
                    .sortedBy { it.ordinal }
                    .firstOrNull()
                    ?.resolve?.invoke(elem.base) ?: throw ArtusPathException(elem.onError("component of path ${succession.subList(0, idx + 1)} not found"))
        })
    }

    /**
     * resolves all possible paths ordered naturally
     */
    fun multiResolve(thisScope: ArtusScope, rootScope: ArtusScope): List<ArtusScope> {
        val scope = if (relative) thisScope else rootScope
        return succession.fold(listOf(scope), {
            acc: List<ArtusScope>, elem: ArtusId ->
            acc.map {
                it.components
                        .filter { it.isAvailableFor(elem.base) }
                        .sortedBy { it.ordinal }
                        .map { it.resolve(elem.base) }
            }.fold(listOf(), { acc, it -> acc + it })
        })
    }
}