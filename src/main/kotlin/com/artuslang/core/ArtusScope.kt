package com.artuslang.core

import com.artuslang.core.component.ArtusId
import com.artuslang.core.component.ArtusScopeResolver
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Frederic on 09/10/2017.
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