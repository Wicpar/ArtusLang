package com.artuslang.core

import java.math.BigInteger

/**
 * Created by Frederic on 09/10/2017.
 */
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