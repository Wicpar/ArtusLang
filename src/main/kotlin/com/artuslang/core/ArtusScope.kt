package com.artuslang.core

import com.artuslang.core.component.ArtusId
import com.artuslang.core.component.ArtusScopeResolver
import java.util.*
import kotlin.collections.ArrayList

/**
* Copyright 2017 Frederic Artus Nieto
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
class ArtusScope(val parent: ArtusScope? = null) {
    val components = ArrayList<(ArtusId<*>) -> ArtusScope?>()
    val structure = ArrayList<ArtusScopeResolver>()

    fun compile(): BitSet {
        val errors = ArrayList<String>()
        structure.map {
            try {
                return@map it.resolve(this).compile()
            } catch (e: ArtusPathException) {
                errors.add(e.msg)
            }
            BitSet()
        }.fold(BitSet(), {
            acc, elem ->
            val ret = BitSet(acc.size() + elem.size())
            acc.size()
            ret
        })
        return BitSet()
    }
}