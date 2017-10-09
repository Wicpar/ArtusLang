/*
 * *
 *  Copyright 2017 Frederic Artus Nieto
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package com.artuslang.core

import java.math.BigInteger

class ArtusBitArray() {
    private var value: BigInteger = BigInteger("0")
    private var size: Int = 0

    fun append(bits: String, base: Int) {
        val increase = base * bits.length
        value += BigInteger(bits, 1 shl base).shiftLeft(size)
        size += increase
    }

    fun append(other: ArtusBitArray) {
        val increase = other.size
        value += other.value.shiftLeft(size)
        size += increase
    }

    operator fun plusAssign(other: ArtusBitArray) {
        append(other)
    }

    fun toString(base: Int): String {
        val ret = value.toString(1 shl base)
        return (String(CharArray(size / base - ret.length) {'0'}) + ret).reversed()
    }

    override fun toString(): String {
        return "ArtusBitArray(value=${value.toString(2)}, size=$size)"
    }

}