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
import com.artuslang.lang.ContextualizedLogger
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
        private val accessorFactories = TreeSet<ArtusScopeAccessorFactory<*>>()
        private val factories = HashMap<Class<*>, TreeSet<ArtusScopeAccessorFactory<*>>>()
        fun registerFactory(factory : ArtusScopeAccessorFactory<*>) {
            accessorFactories.add(factory)
            factory.handledTypes.forEach {
                factories.getOrPut(it, { TreeSet() }).add(factory)
            }
        }
    }

    private val components = HashMap<Class<*>, TreeSet<ArtusScopeAccessor<*>>>()
    private val accessors = HashMap<ArtusScopeAccessorFactory<*>, ArtusScopeAccessor<*>>()

    operator fun get(elem: Any, onError: (String) -> Unit): ArtusScope? {
        components[elem::class.java]?.find { it.isApplicableTo(elem) }?.applyTo(elem)?.let { return it } ?: onError("could not access Scope with \"$elem\"")
        return null
    }

    fun registerScope(elem: Any, onError: (String) -> Unit): ArtusScope? {
        factories[elem::class.java]?.find {
            it.canRegister(elem)
        }?.let {
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

    fun getAccessors(): List<ArtusScopeAccessor<*>> {
        return accessors.values.toList()
    }
}

interface ArtusScopeAccessorFactory<T: ArtusScopeAccessorFactory<T>>: Comparable<ArtusScopeAccessorFactory<T>> {
    val index: Int
    fun canRegister(elem: Any): Boolean
    val handledTypes: List<Class<*>>
    fun newAccessor(): ArtusScopeAccessor<T>
    override fun compareTo(other: ArtusScopeAccessorFactory<T>): Int {
        return index.compareTo(other.index)
    }
}

class ContextualizedObject<T: Any> (val obj: T, val logger: ContextualizedLogger) {
    val type: Class<T> = obj.javaClass
}


open class ArtusLinearFactory(override val index: Int, handledTypes: List<Class<*>>): ArtusScopeAccessorFactory<ArtusLinearFactory> {

    override val handledTypes: List<Class<*>> = handledTypes + ContextualizedObject::class.java

    override fun canRegister(elem: Any): Boolean {
        return when (elem) {
            is ContextualizedObject<*> -> handledTypes.contains(elem.type)
            else -> false
        }
    }

    override fun newAccessor(): ArtusScopeAccessor<ArtusLinearFactory> {
        return InnerScopeAccessor()
    }

    inner class InnerScopeAccessor: LinearScopeAccessor<ArtusLinearFactory>(this, index, {
        elem: Any ->
        val obj = (elem as ContextualizedObject<*>)
        Pair(obj.obj, IdentifiedArtusBasicScope(obj.logger, obj.obj))
    })
}

interface ArtusScopeAccessor<T: ArtusScopeAccessorFactory<T>>: Comparable<ArtusScopeAccessor<T>> {
    val factory: T
    val index: Int
    fun isApplicableTo(elem: Any): Boolean
    fun applyTo(elem: Any): ArtusScope
    fun register(elem: Any): ArtusScope
    fun getRegisteredScopes(): List<ArtusScope>
    override fun compareTo(other: ArtusScopeAccessor<T>): Int {
        return index.compareTo(other.index)
    }
}

abstract class AbstractScopeAccessor<T: ArtusScopeAccessorFactory<T>>(override val factory: T, override val index: Int): ArtusScopeAccessor<T>

open class LinearScopeAccessor<T: ArtusScopeAccessorFactory<T>>(factory: T, index: Int, protected val mapper: (elem: Any) -> Pair<Any, ArtusScope>): AbstractScopeAccessor<T>(factory, index) {
    protected val list = HashMap<Any, ArtusScope>()

    override fun register(elem: Any): ArtusScope {
        val ret = mapper(elem)
        list.put(ret.first, ret.second)
        return ret.second
    }

    override fun isApplicableTo(elem: Any): Boolean {
        return list.containsKey(elem)
    }

    override fun applyTo(elem: Any): ArtusScope {
        return list[elem]!!
    }

    override fun getRegisteredScopes(): List<ArtusScope> {
        return list.values.toList()
    }
}

open class ArtusBasicScope(val logger: ContextualizedLogger) : ArtusScope {

    override fun printErr(err: String) {
        logger.log("severe", err)
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

open class IdentifiedArtusBasicScope(origin: ContextualizedLogger, val identifier: Any) : ArtusBasicScope(origin) {
    override fun toString(): String {
        return identifier.toString()
    }
}