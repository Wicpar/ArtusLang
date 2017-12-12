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

import com.artuslang.lang.matching.Matcher
import com.artuslang.lang.matching.TokenType
import org.apache.commons.jexl3.JexlContext
import org.apache.commons.jexl3.internal.Closure
import org.intellij.lang.annotations.Language

class GlobalRepo(val origin: ArtusContext) {

    companion object {
        private val modules = hashSetOf<Any>()
        private val tokens: HashMap<String, TokenType> = HashMap(LexerDefaults.defaultTokenMap)
        private val matchers: HashMap<String, Matcher> = HashMap(LexerDefaults.defaultMatcherMap)
        private val contextTypes: HashMap<String, ArtusContextType> = HashMap(LexerDefaults.contextMap)
        private val listeners = HashMap<String, ArrayList<(JexlContext, ArtusContextType) -> Unit>>()
        private val utils = HashMap<Any, Any?>()
    }

    fun hasModule(elem: Any): Boolean {
        return modules.contains(elem)
    }

    fun registerModule(elem: Any) {
        modules.add(elem)
    }

    fun tokenTypeOf(str: String): TokenType {
        return tokens.getOrPut(str, { TokenType(str) })
    }

    @JvmOverloads
    fun registerMatcher(str: String, @Language("RegExp") pattern: String, group: Int = 1): Matcher {
        val ret = Matcher(tokenTypeOf(str), pattern, group)
        matchers.put(str, ret)
        return ret
    }

    fun getMatcher(str: String): Matcher? {
        return matchers[str]
    }

    fun onContextRegister(name: String, closure: Closure) {
        listeners.getOrPut(name, { arrayListOf() }).add({ jexl, ctx -> closure.execute(jexl, ctx) })
    }

    @JvmOverloads
    fun preRegisterContextType(str: String, parent: ArtusContextType? = null, entry: Matcher? = null): ArtusContextType {
        return registerContextType(str, entry = entry, parent = parent)
    }

    @JvmOverloads
    fun registerContextType(str: String, entry: Matcher? = null, stack: Array<Matcher> = arrayOf(), actions: Map<TokenType, Any?>? = null, parent: ArtusContextType? = null): ArtusContextType {
        val ret = ArtusContextType(str, entry, ArrayList(stack.asList()), actions ?: mapOf(), parent)
        contextTypes.put(str, ret)
        listeners[str]?.forEach { it.invoke(origin.jexl, ret) }
        return ret
    }

    fun getContextType(str: String): ArtusContextType? {
        return contextTypes[str]
    }

    fun getUtil(obj: Any): Any? {
        return utils[obj]
    }

    fun setUtil(obj: Any, y: Any?): Any? {
        return utils.put(obj, y)
    }
}