package com.artuslang.core.component

/**
 * Created by Frederic on 09/10/2017.
 */
class ArtusId<out T>(val base: T, val onError: (message: String)-> String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArtusId<*>) return false

        if (base != other.base) return false

        return true
    }

    override fun hashCode(): Int {
        return base?.hashCode() ?: 0
    }

    override fun toString(): String {
        return base.toString()
    }

}