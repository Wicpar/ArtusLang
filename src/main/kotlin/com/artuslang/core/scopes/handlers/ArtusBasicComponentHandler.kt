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

package com.artuslang.core.scopes.handlers

import com.artuslang.core.scopes.ArtusScope
import com.artuslang.core.scopes.accessors.ArtusScopeAccessor
import com.artuslang.core.scopes.accessors.factory.ArtusScopeAccessorFactory
import java.util.TreeSet
import kotlin.collections.HashMap

open class ArtusBasicComponentHandler : ArtusComponentHandler {

    private val components = HashMap<Class<*>, TreeSet<ArtusScopeAccessor<*>>>()
    private val accessors = HashMap<ArtusScopeAccessorFactory<*>, ArtusScopeAccessor<*>>()

    override operator fun get(elem: Any, onError: (String) -> Unit): ArtusScope? {
        return getOrSuperComponent(elem::class.java, {
            it.fold(null as ArtusScope?, { acc, it -> acc ?: it.applyToOrNull(elem) })?.let { return@getOrSuperComponent it }
                    ?: onError("could not access Scope with \"$elem\"")
            return@getOrSuperComponent null
        })
    }

    private fun getOrSuperComponent(clazz: Class<*>, onFind: (TreeSet<ArtusScopeAccessor<*>>) -> ArtusScope?): ArtusScope? {
        tailrec fun getSupered(clazz: Class<*>, lst: ArrayList<Class<*>> = arrayListOf()): List<Class<*>> {
            return getSupered(clazz.superclass, lst.apply { add(clazz) })
        }
        fun getInterfaced(clazz: Class<*>, hs: HashSet<Class<*>> = hashSetOf()): HashSet<Class<*>> {
            // prevent infinite loop in case of loop inheritance
            clazz.interfaces.map {
                if (!hs.contains(it)) {
                    hs.add(it)
                    getInterfaced(it, hs)
                }
            }
            return hs
        }
        return getSupered(clazz).fold(null as ArtusScope?, { acc, it -> acc ?: components[it]?.let(onFind) }) ?:
                getInterfaced(clazz).fold(null as ArtusScope?, { acc, it -> acc ?: components[it]?.let(onFind) })
    }

    override fun registerScope(elem: Any, onError: (String) -> Unit): ArtusScope? {
        ArtusComponentHandler[elem::class.java]?.find {
            it.canRegister(elem)
        }?.let {
            return accessors.getOrPut(it, {
                val accessor = it.newAccessor()
                accessor.factory.handledTypes.forEach {
                    components.getOrPut(it, { TreeSet() }).add(accessor)
                }
                accessor
            }).register(elem)
        } ?: onError("could not register Scope with \"$elem\"")
        return null
    }

    override fun getAccessors(): List<ArtusScopeAccessor<*>> {
        return accessors.values.toList()
    }

    override fun getAccessor(factory: ArtusScopeAccessorFactory<*>): ArtusScopeAccessor<*>? {
        return accessors[factory]
    }
}