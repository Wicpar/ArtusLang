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

package com.artuslang.lang

import com.artuslang.core.ArtusBitArray
import com.artuslang.core.component.ArtusId
import com.artuslang.core.scopes.ArtusScope
import com.artuslang.core.scopes.EndScope
import com.artuslang.lang.matching.LexerToken
import org.apache.commons.jexl3.JexlContext
import org.apache.commons.jexl3.ObjectContext

class ArtusContext(val type: ArtusContextType, val lexer: ArtusLexer, val scope: ArtusScope, previousContext: ArtusContext?) {
    /**
     * only for actions
     */
    lateinit var token: LexerToken
    val logger: ContextualizedLogger
        get() = LexerLogger(lexer, token)
    val repo = GlobalRepo(this)

    val jexl: JexlContext = ObjectContext(JEXLConfiguration.jexl, this)

    fun eval(str: String): Any? {
        return JEXLConfiguration.jexl.createScript(str).execute(jexl)
    }

    fun clazz(str: String): Class<*>? {
        return Class.forName(str)
    }

    fun log(level: String, obj: Any?) {
        logger.log(level, obj)
    }

    fun endScopeOf(arr: ArtusBitArray): EndScope {
        return EndScope(arr, this)
    }

    @JvmOverloads
    fun subScopeOf(elem: Any, scope: ArtusScope = this.scope): ArtusScope? {
        return scope.components.registerScope(elem, {log("severe", it)})
    }

    fun <T: Any> id(elem: T): ArtusId<T> {
        return ArtusId(elem, LexerLogger(lexer, token))
    }

    fun bitArrayOf(str: String, base: Int): ArtusBitArray {
        return ArtusBitArray(str, base)
    }

    fun bitArrayOf(str: String): ArtusBitArray {
        return ArtusBitArray(str)
    }

    val doNothing = object: ArtusContextType.ContextAction {
        override fun run(ctx: ArtusContext) {}
    }


    private val previousProperties: Map<String, Any> = previousContext?.previousProperties?.plus(previousContext.properties) ?: mapOf()

    private val properties =  HashMap<String, Any>()

    fun setProperty(property: String, content: Any?) {
        if (content != null)
            lexer.context.properties.put(property, content)
        else
            lexer.context.properties.remove(property)
    }

    @JvmOverloads
    fun getProperty(property: String, inherit: Boolean = true): Any? {
        return lexer.context.properties[property] ?: if (inherit) lexer.context.previousProperties[property] else null
    }
}

