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

package com.artuslang.core.scopes.accessors

import com.artuslang.core.ContextualizedScopeBuilder
import com.artuslang.core.component.ArtusId
import com.artuslang.core.scopes.ArtusScope
import com.artuslang.core.scopes.accessors.factory.ArtusScopeAccessorFactory
import java.util.*
import kotlin.collections.HashMap

open class ProceduralScopeAccessor<T: ArtusScopeAccessorFactory<T>>(factory: T, index: Int): AbstractScopeAccessor<T>(factory, index) {

    protected val list = TreeSet<ContextualizedScopeBuilder>()
    protected val map = HashMap<Any, ArtusScope>()

    override fun applyToOrNull(elem: Any): ArtusScope? {
        fun mapScopes(lst: Iterable<*>): Map<Any, ArtusScope>? {
            return lst.mapIndexed { index, it ->
                when (it) {
                    is ArtusId<*> -> Pair(index, it.base as? ArtusScope ?: return null)
                    else -> Pair(index, it as? ArtusScope ?: return null)
                }
            }.associate { it }
        }
        fun mapScopes(lst: Map<*, *>): Map<Any, ArtusScope>? {
            return lst.map {
                val v = it.value
                when (v) {
                    is ArtusId<*> -> Pair(it.key ?: return null, v.base as? ArtusScope ?: return null)
                    else -> Pair(it.key ?: return null, v as? ArtusScope ?: return null)
                }
            }.associate { it }
        }
        val map: Map<Any, ArtusScope> = when (elem) {
            is ArtusId<*> -> if (elem.base is List<*>) {
                mapScopes(elem.base) ?: return null
            } else return null
            is Iterable<*> -> mapScopes(elem) ?: return null
            is Array<*> -> mapScopes(elem.asIterable()) ?: return null
            is ArtusScope -> mapOf(Pair(0, elem))
            is Map<*, *> -> mapScopes(elem) ?: return null
            else -> return null
        }
        val ret = this.map[map] ?: list.fold(null as ArtusScope?, { acc, builder ->
            if (acc != null) return@fold acc
            val remap = map.mapKeys { builder.aliases[it.key] ?: it.key }
            if (builder.filter(remap)) {
                builder.builder(remap, builder.scope)
            } else {
                null
            }
        }) ?: return null
        this.map.put(map, ret)
        return ret
    }

    override fun register(elem: Any): ArtusScope {
        elem as ContextualizedScopeBuilder
        map.clear()
        list.add(elem)
        return elem.scope
    }

    override fun getRegisteredScopes(): List<ArtusScope> {
        return list.map { it.scope }
    }

}