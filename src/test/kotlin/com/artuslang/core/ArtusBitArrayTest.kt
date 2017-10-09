package com.artuslang.core

import org.junit.Test

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
class ArtusBitArrayTest {

    @Test
    fun shifting() {
        val arr = ArtusBitArray()
        arr.append("1111", 1)
        println(arr.toString(1))
        println(arr)
        arr.append("0000", 1)
        println(arr.toString(1))
        println(arr)
        val arr2 = ArtusBitArray()
        arr2.append("FF", 4)
        println(arr2.toString(4))
        println(arr2)
        arr2.append("00", 4)
        println(arr2.toString(4))
        println(arr2)
        arr.append(arr2)
        println(arr.toString(1))
    }
}