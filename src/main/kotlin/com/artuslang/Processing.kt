/*
 * Copyright 2018 - present Frederic Artus Nieto
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

package com.artuslang

import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlContext
import org.apache.commons.jexl3.internal.Closure
import org.apache.commons.jexl3.introspection.JexlSandbox
import org.intellij.lang.annotations.Language
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
 * Created on 22/01/2018 by Frederic
 */

open class NamedType(val name: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NamedType) return false

        if (name != other.name) return false
        if (this.javaClass != other.javaClass) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

data class ArtusLocation(val origin: String, val range: IntRange)

class TokenType(name: String, val properties: HashMap<String, Any?>) : NamedType(name)

data class Token(val location: ArtusLocation, val text: String, val type: TokenType)

data class TokenMatcher(val type: TokenType, @Language("RegExp") val pattern: String, val group: Int = 1) {
    val regex = Regex("\\A($pattern)", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL, RegexOption.UNIX_LINES))
}

class ContextMatcher(val matcher: TokenMatcher, val event: (Token, Context) -> Context)

class ContextType(name: String, private val matchers: ArrayList<ContextMatcher>, private val parents: ArrayList<ContextType> = arrayListOf()) : NamedType(name) {

    private val stack: ArrayList<ContextMatcher> = ArrayList(matchers + parents.map { it.stack }.flatten())
    private val classTypes: HashSet<ContextType> = HashSet(parents.map { it.classTypes }.flatten() + this)
    private val stringTypes: HashSet<String> = HashSet(classTypes.map { it.name })

    @JvmOverloads
    fun addParent(ctx: ContextType, idx: Int = 0) {
        val i = if (idx < 0) idx + matchers.size + 1 else idx
        parents.add(i, ctx)
        stack.clear()
        stack.addAll(matchers + parents.map { it.stack }.flatten())
        classTypes.addAll(ctx.classTypes)
        stringTypes.addAll(ctx.stringTypes)
    }

    @JvmOverloads
    fun addMatcher(ctx: ContextMatcher, idx: Int = 0) {
        val i = if (idx < 0) idx + matchers.size + 1 else idx
        matchers.add(i, ctx)
        stack.add(i, ctx)
    }

    fun getStack(): List<ContextMatcher> {
        return stack
    }

    fun isType(contextType: ContextType): Boolean {
        return classTypes.contains(contextType)
    }

    fun isType(contextType: String): Boolean {
        return stringTypes.contains(contextType)
    }
}

class Context(val type: ContextType, val parent: Context? = null) {
    val children = HashMap<ContextType, ArrayList<Context>>()
    val properties = HashMap<String, Any?>()
    val tokens = ArrayList<Token>()

    init {
        if (parent != null)
            properties.putAll(parent.properties)
    }

    fun pushToken(token: Token) {
        tokens.add(token)
        parent?.pushToken(token)
    }

    fun child(contextType: ContextType): Context {
        val ctx = Context(contextType, this)
        children.getOrPut(contextType, { arrayListOf() }).add(ctx)
        return ctx
    }

    @JvmOverloads
    fun parent(type: ContextType, deep: Boolean = true): Context? {
        var parent = parent
        if (deep) {
            while (parent != null && !parent.type.isType(type))
                parent = parent.parent
        } else {
            while (parent != null && parent.type != type)
                parent = parent.parent
        }
        return parent
    }

    @JvmOverloads
    fun parent(type: String, deep: Boolean = true): Context? {
        var parent = parent
        if (deep) {
            while (parent != null && !parent.type.isType(type))
                parent = parent.parent
        } else {
            while (parent != null && parent.type.name != type)
                parent = parent.parent
        }
        return parent
    }
}

class ScriptContext : JexlContext, JexlContext.NamespaceResolver {

    private val fixedNamespaces = mapOf(Pair("this", this), Pair("lang", LangUtils(this)), Pair("log", Log))
    private val namespaces = hashMapOf<String?, Any?>()
    private val map = hashMapOf<String?, Any?>()

    override fun has(name: String?): Boolean {
        return map.containsKey(name)
    }

    override fun get(name: String?): Any? {
        return map[name]
    }

    override fun set(name: String?, value: Any?) {
        if (value == null)
            map.remove(name)
        else
            map[name] = value
    }

    fun put(name: String, value: Any?): Any? {
        return if (value == null)
            map.remove(name)
        else
            map.put(name, value)
    }

    override fun resolveNamespace(name: String?): Any? {
        return fixedNamespaces[name] ?: namespaces[name]
    }

    fun registerNamespace(str: String, obj: Any?) {
        namespaces[str] = obj
    }
}

class LangUtils(private val ctx: ScriptContext) {
    fun tokenType(name: String, properties: HashMap<String, Any?>) = TokenType(name, properties)

    @JvmOverloads
    fun tokenMatcher(type: TokenType, @Language("RegExp") pattern: String, group: Int = 1) = TokenMatcher(type, pattern, group)

    fun contextMatcher(matcher: TokenMatcher, event: Closure) = ContextMatcher(matcher, { token, context ->
        val tmpctx = ctx.put("context", context)
        val ret = event.execute(ctx, token, context) as Context
        ctx.put("context", tmpctx)
        ret
    })

    fun contextMatcherPush(matcher: TokenMatcher, type: ContextType) = ContextMatcher(matcher, { _, context -> context.child(type) })
    fun contextMatcherPop(matcher: TokenMatcher) = ContextMatcher(matcher, { _, context -> context.parent!! })
    fun contextMatcherPopWith(matcher: TokenMatcher, closure: Closure) = ContextMatcher(matcher, { token, context ->
        val tmpctx = ctx.put("context", context)
        closure.execute(ctx, token, context)
        val ret = context.parent!!
        ctx.put("context", tmpctx)
        ret
    })

    fun contextMatcherSwitch(matcher: TokenMatcher, type: ContextType) = ContextMatcher(matcher, { _, context ->
        context.parent?.child(type) ?: Context(type)
    })

    fun contextMatcherNop(matcher: TokenMatcher) = ContextMatcher(matcher, { _, context -> context })

    @JvmOverloads
    fun contextType(name: String, matchers: ArrayList<ContextMatcher>, parents: ArrayList<ContextType> = ArrayList()) = ContextType(name, matchers, parents)

    fun context(type: ContextType) = Context(type)

    @JvmOverloads
    fun readFile(path: String, charset: String = Charset.defaultCharset().name()): ArtusReader {
        return FileArtusReader(File(path), charset)
    }

    fun writeFile(path: String, buffer: ByteBuffer) {
        val file = File(path)
        val dir = File(".")
        if (!file.canonicalPath.contains(dir.canonicalPath + File.separator)) {
            throw RuntimeException("illegal file access, only children of local folder allowed")
        }
        file.mkdirs()
        buffer.reset()
        val arr = ByteArray(buffer.remaining())
        buffer.get(arr)
        buffer.clear()
        file.writeBytes(arr)
    }

    fun readString(str: String, name: String): ArtusReader {
        return StringArtusReader(str, name)
    }

    fun eval(code: String, vararg v: Pair<String, Any?>): Any? {
        val tmp = v.map { Pair(it.first, ctx.put(it.first, it.second)) }
        val ret =  jexl.createScript(code).execute(ctx)
        tmp.forEach { ctx.put(it.first, it.second) }
        return ret
    }

    @JvmOverloads
    fun import(path: String, ctx: ContextType, charset: String = Charset.defaultCharset().name()) {
        val fs = File(path)
        val file = this.ctx.put("file", fs.path)
        val folder = this.ctx.put("folder", fs.parentFile.path)
        try {
            readFile(fs.path, charset).build(Context(ctx))
        } catch (t: Throwable) {
            System.err.println("error: $fs: $t")
            throw t
        } finally {
            this.ctx.put("file", file)
            this.ctx.put("folder", folder)
        }
    }

    fun data() = Data()

    /**
     * easy constructors for allowed types
     */
    fun listOf(vararg any: Any?) = listOf<Any?>(*any)

    fun arrayListOf(vararg any: Any?) = arrayListOf<Any?>(*any)
    fun mapOf(vararg any: Pair<Any?, Any?>) = mapOf<Any?, Any?>(*any)
    fun hashMapOf(vararg any: Pair<Any?, Any?>) = hashMapOf<Any?, Any?>(*any)
    fun pairOf(a: Any?, b: Any?) = Pair(a, b)
}

object Log {
    fun println(any: Any) = kotlin.io.println(any)
    fun tokenErr(token: Token, msg: Any) = println("${token.location.origin}:${token.location.range}: token \"${token.text}\" error: $msg")
}

interface ArtusReader {
    val name: String
    fun build(ctx: Context): Context
}

class FileArtusReader(file: File, charset: String = Charset.defaultCharset().name()) : StringArtusReader({
    val dir = File(".")
    if (!file.canonicalPath.contains(dir.canonicalPath + File.separator)) {
        throw RuntimeException("$file: illegal file access, only children of local folder allowed")
    }
    file.readText(Charset.forName(charset))
}(), file.path)

open class StringArtusReader(val str: String, override val name: String) : ArtusReader {
    private var data = str
    private var offset = 0

    override fun build(ctx: Context): Context {
        var ctx: Context = ctx
        while (data.isNotEmpty()) {
            ctx = ctx.type.getStack().fold(null as Context?, { acc, contextMatcher ->
                acc ?: {
                    val matched = contextMatcher.matcher.regex.find(data)
                    matched?.groups?.get(contextMatcher.matcher.group)?.let {
                        val off = it.range.endInclusive + 1
                        data = data.substring(off)
                        val token = Token(ArtusLocation(name, IntRange(offset, offset + off - 1)), it.value, contextMatcher.matcher.type)
                        offset += off
                        ctx.pushToken(token)
                        contextMatcher.event(token, ctx)
                    }
                }()
            }) ?: throw RuntimeException("no Context in source $name at $offset: \"${data.subSequence(0, 20)}...\"")
        }
        return ctx
    }
}

val jexl = JexlBuilder().sandbox({
    val sandbox = JexlSandbox(false)
    val reflect = Reflections("com.artuslang", SubTypesScanner(false))
    reflect.getSubTypesOf(Object::class.java).forEach {
        sandbox.white(it.name)
    }
    sandbox.white(String::class.java.name)
    sandbox.white(Map::class.java.name)
    sandbox.white(List::class.java.name)
    sandbox.white(HashMap::class.java.name)
    sandbox.white(ArrayList::class.java.name)
    sandbox.white(Pair::class.java.name)
    sandbox
}()).create()

/**
 * use like ByteBuffer but flexible size, bytebuffers are based on capacity and not limit
 */
class Data {
    private val buf = arrayListOf<ByteGroup>()
    private var size = 0

    fun allocate(size: Int): ByteBuffer {
        val ret = ByteBuffer.allocateDirect(size)
        add(ret)
        return ret
    }

    fun add(buffer: ByteBuffer) {
        val group = ArrayByteGroup(buffer, size)
        buf.add(group)
        size += group.size
    }

    fun toByteBuffer(): ByteBuffer {
        val ret = ByteBuffer.allocate(size)
        buf.forEach {
            val dat = it.dat
            dat.clear()
            ret.put(dat)
        }
        ret.clear()
        return ret
    }
}

interface ByteGroup {
    val size: Int
    val offset: Int
    val dat: ByteBuffer
}

class ArrayByteGroup(override val dat: ByteBuffer, override val offset: Int): ByteGroup {
    override val size = dat.capacity()
}

class NDefTree() {

    private val root = NDefNode()
    private val symmetricalTypes = arrayListOf<Class<*>>()

    private inner class NDefNode() {

    }

    fun symmetrcalPathType(type: Class<*>) {
        symmetricalTypes.add(type)
    }
}

/**
 * [path] the path to follow nodes
 * [features] the path will priorize ambiguities with the most matching features, features defined with lower indexes have priority
 * [filters] the path will not follow any node that is missing a filter, following the one with the least superfluous filters
 */
class NDefPath(val path: List<Any>, val features: List<Any> = listOf(), val filters: List<Any> = listOf())