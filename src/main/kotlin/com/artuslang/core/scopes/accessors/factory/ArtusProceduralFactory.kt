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

import com.artuslang.core.ContextualizedScopeBuilder
import com.artuslang.core.component.ArtusId
import com.artuslang.core.scopes.accessors.ArtusScopeAccessor
import com.artuslang.core.scopes.accessors.ProceduralScopeAccessor

open class ArtusProceduralFactory(override val index: Int): ArtusScopeAccessorFactory<ArtusProceduralFactory> {

    override fun canRegister(elem: Any): Boolean {
        return elem is ContextualizedScopeBuilder
    }

    override fun newAccessor(): ArtusScopeAccessor<ArtusProceduralFactory> {
        return InnerScopeAccessor()
    }

    inner class InnerScopeAccessor : ProceduralScopeAccessor<ArtusProceduralFactory>(this, index)

    override val handledTypes: List<Class<*>> = listOf(ArtusId::class.java, ContextualizedScopeBuilder::class.java)
}