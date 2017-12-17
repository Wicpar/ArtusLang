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

import com.artuslang.core.component.ArtusScopeResolver
import com.artuslang.lang.ArtusContext
import java.util.TreeSet
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

interface ArtusScope {
    fun compile(lastState: ArtusBitArray): ArtusBitArray
    val structure: List<ArtusScopeResolver>
    val components: ArtusComponentHandler
    fun printErr(err: String)
}

class ArtusComponentHandler {
    companion object {
        private val accessorFactories = TreeSet<ArtusScopeAcessorFactory<*, *>>()
        private val factories = HashMap<Class<*>, TreeSet<ArtusScopeAcessorFactory<*, *>>>()
        fun registerFactory(factory : ArtusScopeAcessorFactory<*, *>) {
            accessorFactories.add(factory)
            factory.handledTypes.forEach {
                factories.getOrPut(it, {TreeSet()}).add(factory)
            }
        }
    }

    private val components = HashMap<Class<*>, TreeSet<ArtusScopeAcessor<*, *>>>()
    private val accessors = HashMap<ArtusScopeAcessorFactory<*, *>, ArtusScopeAcessor<*, *>>()

    operator fun get(elem: Any, onError: (String) -> Unit): ArtusScope? {
        components[elem::class.java]?.find { it.factory.isApplicableTo(elem) }?.applyTo(elem)?.let { return it } ?: onError("could not access Scope with \"$elem\"")
        return null
    }

    fun registerScope(elem: Any, onError: (String) -> Unit): ArtusScope? {
        factories[elem::class.java]?.find { it.canRegister(elem) }?.let {
            return accessors.getOrPut(it, {
                val accessor = it.newAccessor()
                accessor.factory.handledTypes.forEach {
                    components.getOrPut(it, {TreeSet()}).add(accessor)
                }
                accessor
            }).register(elem)
        } ?: onError("could not register Scope with \"$elem\"")
        return null
    }

    fun getAccessors(): List<ArtusScopeAcessor<*, *>> {
        return accessors.values.toList()
    }
}

interface ArtusScopeAcessorFactory<T: ArtusScopeAcessorFactory<T, U>, U: ArtusScopeAcessor<T, U>>: Comparable<T> {
    fun isApplicableTo(elem: Any): Boolean
    fun canRegister(elem: Any): Boolean
    val handledTypes: List<Class<*>>
    fun newAccessor(): U
}

interface ArtusScopeAcessor<T: ArtusScopeAcessorFactory<T, U>, U: ArtusScopeAcessor<T, U>>: Comparable<U> {
    val factory: ArtusScopeAcessorFactory<T, U>
    fun applyTo(elem: Any): ArtusScope
    fun register(elem: Any): ArtusScope
    fun getRegisteredScopes(): List<ArtusScope>
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

open class ArtusBasicScope(val origin: ArtusContext) : ArtusScope {

    override fun printErr(err: String) {
        origin.log("severe", err)
    }

    override val structure = ArrayList<ArtusScopeResolver>()
    override val components = ArtusComponentHandler()

    override fun compile(lastState: ArtusBitArray): ArtusBitArray {
        val ret: ArtusBitArray = structure.fold(ArtusBitArray(), { acc, it ->
            try {
                it.resolve(this).compile(acc)
            } catch (e: Exception) {
                printErr(e.toString())
                ArtusBitArray()
            }
        })
        return lastState.append(ret)
    }
}