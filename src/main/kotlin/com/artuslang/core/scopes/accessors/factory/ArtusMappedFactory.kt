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

package com.artuslang.core.scopes.accessors.factory

import com.artuslang.core.scopes.ArtusScope
import com.artuslang.core.scopes.IdentifiedScope
import com.artuslang.core.scopes.accessors.ArtusScopeAccessor
import com.artuslang.core.scopes.accessors.MappedScopeAccessor

/**
 * Created on 27/12/2017 by Frederic
 */
class ArtusMappedFactory(override val index: Int) : ArtusScopeAccessorFactory<ArtusMappedFactory> {

    override val handledTypes: List<Class<*>> = listOf(Map.Entry::class.java, Pair::class.java, IdentifiedScope::class.java)

    override fun canRegister(elem: Any): Boolean {
        return when (elem) {
            is Pair<*, *> -> elem.first != null && elem.second is ArtusScope
            is Map.Entry<*, *> -> elem.key != null && elem.value is ArtusScope
            else -> elem is IdentifiedScope
        }
    }

    override fun newAccessor(): ArtusScopeAccessor<ArtusMappedFactory> {
        return InnerScopeAccessor()
    }

    inner class InnerScopeAccessor : MappedScopeAccessor<ArtusMappedFactory>(this, index)
}