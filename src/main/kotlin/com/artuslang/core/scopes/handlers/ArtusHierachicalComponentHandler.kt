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

open class ArtusHierachicalComponentHandler(val parents: List<ArtusComponentHandler> = listOf()) : ArtusBasicComponentHandler() {

    fun getHere(elem: Any, onError: (String) -> Unit): ArtusScope? {
        return super.get(elem, onError)
    }

    override operator fun get(elem: Any, onError: (String) -> Unit): ArtusScope? {
        return super.get(elem, onError) ?: parents.fold(null as ArtusScope?, { acc, artusComponentHandler ->
            acc ?: if (artusComponentHandler is ArtusHierachicalComponentHandler)
                artusComponentHandler.getHere(elem, onError)
            else
                artusComponentHandler[elem, onError]
        })
    }

    override fun getAccessors(): List<ArtusScopeAccessor<*>> {
        return super.getAccessors() + parents.fold(listOf<ArtusScopeAccessor<*>>(), { acc, artusComponentHandler -> acc + artusComponentHandler.getAccessors() })
    }
}