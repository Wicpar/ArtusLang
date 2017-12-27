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

import com.artuslang.core.component.ArtusId
import com.artuslang.core.scopes.IdentifiedArtusBasicScope
import com.artuslang.core.scopes.accessors.ArtusScopeAccessor
import com.artuslang.core.scopes.accessors.LinearScopeAccessor

open class ArtusLinearFactory(override val index: Int, handledTypes: List<Class<*>>) : ArtusScopeAccessorFactory<ArtusLinearFactory> {

    override val handledTypes: List<Class<*>> = handledTypes + ArtusId::class.java

    override fun canRegister(elem: Any): Boolean {
        return when (elem) {
            is ArtusId<*> -> handledTypes.contains(elem.base.javaClass)
            else -> false
        }
    }

    override fun newAccessor(): ArtusScopeAccessor<ArtusLinearFactory> {
        return InnerScopeAccessor()
    }

    inner class InnerScopeAccessor : LinearScopeAccessor<ArtusLinearFactory>(this, index, { elem: Any ->
        val obj = (elem as ArtusId<*>)
        Pair(obj.base, IdentifiedArtusBasicScope(obj.logger, obj.base))
    })
}