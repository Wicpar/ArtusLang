package com.artuslang.core

import com.artuslang.core.component.ArtusId
import com.artuslang.core.component.ArtusScopeResolver

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
open class ArtusScope(val parent: ArtusScope? = null) {

    companion object {
        fun getParentId(onError: (String) -> String): ArtusId<Relative> = ArtusId(Relative.PARENT, onError)
    }

    enum class Relative {
        PARENT
    }

    val components = arrayListOf<(ArtusId<*>) -> ArtusScope?>({ if (it.base === Relative.PARENT) parent else null})
    val structure = ArrayList<ArtusScopeResolver>()

    open fun compile(): ArtusBitArray {
        val errors = ArrayList<String>()
        val ret = structure.map {
            try {
                return@map it.resolve(this).compile()
            } catch (e: ArtusPathException) {
                errors.add(e.msg)
            }
            ArtusBitArray()
        }.fold(ArtusBitArray(), {
            acc, elem ->
            acc.append(elem)
            acc
        })
        if (errors.size > 0)
            throw ArtusPathMultiException(errors)
        return ret
    }
}