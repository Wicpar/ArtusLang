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

import com.artuslang.core.component.ArtusId
import com.artuslang.core.component.ArtusScopeComponent
import com.artuslang.core.component.ArtusScopeResolver

open class ArtusScope(open val parent: ArtusScope? = null) {

    companion object {
        fun getParentId(onError: (String) -> String): ArtusId<Relative> = ArtusId(Relative.PARENT, onError)
    }

    enum class Relative {
        PARENT
    }

    val components = arrayListOf(ArtusScopeComponent({parent != null && it.base === Relative.PARENT}, 0, { parent!! }))
    val structure = ArrayList<ArtusScopeResolver>()


    open fun compile(lastState: ArtusBitArray): ArtusBitArray {
        val errors = ArrayList<String>()
        val ret: ArtusBitArray = structure.fold(ArtusBitArray(), {
            acc, it ->
            try {
                it.resolve(this).compile(acc)
            } catch (e: ArtusPathException) {
                errors.add(e.msg)
                ArtusBitArray()
            }
        })
        if (errors.size > 0)
            throw ArtusPathMultiException(errors)
        return lastState.append(ret)
    }
}