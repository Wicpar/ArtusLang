package com.artuslang.core.component

import com.artuslang.core.ArtusPathException
import com.artuslang.core.ArtusScope

/**
 * Created by Frederic on 09/10/2017.
 */
class ArtusPath(val relative: Boolean, val succession: List<ArtusId<*>>) {

    fun resolve(thisScope: ArtusScope, rootScope: ArtusScope): ArtusScope {
        val scope = if (relative) thisScope else rootScope
        return succession.foldIndexed(scope, {
            idx: Int, acc: ArtusScope, elem: ArtusId<*> ->
            acc.components
                    .map { it(elem) }
                    .filter { it != null }
                    .firstOrNull() ?: throw ArtusPathException(elem.onError("component of path ${succession.subList(0, idx + 1)} not found"))
        })
    }
}