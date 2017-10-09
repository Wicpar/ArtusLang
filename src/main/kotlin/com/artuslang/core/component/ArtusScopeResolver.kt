package com.artuslang.core.component

import com.artuslang.core.ArtusScope

/**
 * Created by Frederic on 09/10/2017.
 */
class ArtusScopeResolver(val path: ArtusPath, val rootScope: ArtusScope) {
    fun resolve(relative: ArtusScope): ArtusScope {
        return path.resolve(relative, rootScope)
    }
}