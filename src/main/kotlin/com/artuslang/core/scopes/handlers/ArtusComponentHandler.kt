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

interface ArtusComponentHandler {
    companion object {
        private val accessorFactories = TreeSet<ArtusScopeAccessorFactory<*>>()
        operator fun get(clazz: Class<*>): TreeSet<ArtusScopeAccessorFactory<*>>? {
            return factories[clazz]
        }

        protected val factories = HashMap<Class<*>, TreeSet<ArtusScopeAccessorFactory<*>>>()
        fun registerFactory(factory: ArtusScopeAccessorFactory<*>) {
            accessorFactories.add(factory)
            factory.handledTypes.forEach {
                factories.getOrPut(it, { TreeSet() }).add(factory)
            }
        }
    }

    operator fun get(elem: Any, onError: (String) -> Unit): ArtusScope?
    fun registerScope(elem: Any, onError: (String) -> Unit): ArtusScope?
    fun getAccessors(): List<ArtusScopeAccessor<*>>
    fun getAccessor(factory: ArtusScopeAccessorFactory<*>): ArtusScopeAccessor<*>?
}