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

package com.artuslang.el

import com.artuslang.lang.JEXLConfiguration
import org.apache.commons.jexl3.JexlException
import org.apache.commons.jexl3.MapContext
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test



/**
 * Created on 06/12/2017 by Frederic
 */
internal class ELTest {

    val jexl = JEXLConfiguration.jexl

    @Test
    fun testExit() {
        assertThrows(JexlException::class.java, {
            val jexlExp2 = """
            |class="".class;
            |clazz = class.forName('java.lang.System');
            |m = clazz.methods; m[0].invoke(null, 1); c
            |""".trimMargin()
            val e2 = jexl.createScript(jexlExp2)
            val o2 = e2.execute(MapContext())
            println(o2)
        })
    }

    @Test
    fun testString() {
        val jexlExp1 = """
            |new("java.lang.String", 'hello' + foo);
            |""".trimMargin()
        val e1 = jexl.createScript(jexlExp1)
        val jc = MapContext()
        jc.set("foo", "hello")
        val o = e1.execute(jc)
        println(o)
    }

}