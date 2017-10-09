package com.artuslang.core

import org.junit.Test

/**
 * Created by Frederic on 09/10/2017.
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