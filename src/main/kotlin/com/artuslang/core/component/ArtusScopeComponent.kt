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

import com.artuslang.core.ArtusScope

/**
 * Created on 10/10/2017 by Frederic
 */
open class ArtusScopeComponent (
        val isAvailableFor: (String) -> Boolean,
        val ordinal: Int,
        val resolve: (String) -> ArtusScope
) : Comparable<ArtusScopeComponent> {
    override fun compareTo(other: ArtusScopeComponent): Int {
        return ordinal.compareTo(other.ordinal)
    }
}