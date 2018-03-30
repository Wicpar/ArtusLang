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

import com.google.common.collect.Iterators
import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlContext
import org.apache.commons.jexl3.internal.Closure
import org.apache.commons.jexl3.introspection.JexlSandbox
import org.apache.commons.lang.StringEscapeUtils
import org.intellij.lang.annotations.Language
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.io.File
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.math.min

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
    override fun toString(): String {
        return "TokenMatcher(type=$type, pattern='$pattern', group=$group)"
    }

}

class ContextMatcher(val matcher: TokenMatcher, val event: (Token, Context) -> Context) {
    override fun toString(): String {
        return "ContextMatcher(matcher=$matcher)"
    }
}

class ContextType(name: String, private val matchers: ArrayList<ContextMatcher>, private val parents: ArrayList<ContextType> = arrayListOf()) : NamedType(name) {

    private val parentStacks: ArrayList<LayeredIterable<ContextMatcher>> = ArrayList(parents.map { it.stack })
    private val stack: LayeredIterable<ContextMatcher> = LayeredIterable(matchers, parentStacks)
    private val classTypes: HashSet<ContextType> = HashSet(parents.map { it.classTypes }.flatten() + this)
    private val stringTypes: HashSet<String> = HashSet(classTypes.map { it.name })

    @JvmOverloads
    fun addParent(ctx: ContextType, idx: Int = 0) {
        val i = if (idx < 0) idx + matchers.size + 1 else idx
        parents.add(i, ctx)
        parentStacks.add(i, ctx.stack)
        classTypes.addAll(ctx.classTypes)
        stringTypes.addAll(ctx.stringTypes)
    }

    @JvmOverloads
    fun addMatcher(ctx: ContextMatcher, idx: Int = 0) {
        val i = if (idx < 0) idx + matchers.size + 1 else idx
        matchers.add(i, ctx)
    }

    fun getStack(): Iterable<ContextMatcher> {
        return stack
    }

    fun isType(contextType: ContextType): Boolean {
        return classTypes.contains(contextType)
    }

    fun isType(contextType: String): Boolean {
        return stringTypes.contains(contextType)
    }

    override fun toString(): String {
        return "ContextType(matchers=$matchers, parents=$parents)"
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

    override fun toString(): String {
        return "Context(type=$type, parent=$parent, children=$children, properties=$properties)"
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
    @JvmOverloads
    fun tokenType(name: String, properties: HashMap<String, Any?> = HashMap()) = TokenType(name, properties)

    @JvmOverloads
    fun tokenMatcher(type: TokenType, @Language("RegExp") pattern: String, group: Int = 1) = TokenMatcher(type, pattern, group)

    fun contextMatcher(matcher: TokenMatcher, event: Closure) = ContextMatcher(matcher, { token, context ->
        val ret = event.execute(ctx, token, context) as? Context
        ret ?: context
    })

    fun contextMatcherPush(matcher: TokenMatcher, type: ContextType) = ContextMatcher(matcher, { _, context -> context.child(type) })
    fun contextMatcherPop(matcher: TokenMatcher) = ContextMatcher(matcher, { _, context -> context.parent!! })
    fun contextMatcherPopWith(matcher: TokenMatcher, closure: Closure) = ContextMatcher(matcher, { token, context ->
        closure.execute(ctx, token, context)
        val ret = context.parent!!
        ret
    })

    fun contextMatcherSwitch(matcher: TokenMatcher, type: ContextType) = ContextMatcher(matcher, { _, context ->
        context.parent?.child(type) ?: Context(type)
    })

    fun contextMatcherSwitchWith(matcher: TokenMatcher, type: ContextType, closure: Closure) = ContextMatcher(matcher, { token, context ->
        val ret = context.parent?.child(type) ?: Context(type)
        closure.execute(ctx, token, context, ret)
        ret
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
        val arr = ByteArray(buffer.remaining())
        buffer.get(arr)
        file.parentFile?.mkdirs()
        file.createNewFile()
        file.writeBytes(arr)
    }

    fun readString(str: String, name: String): ArtusReader {
        return StringArtusReader(str, name)
    }

    fun eval(code: String, vararg v: Pair<String, Any?>): Any? {
        val tmp = v.map { Pair(it.first, ctx.put(it.first, it.second)) }
        val ret = jexl.createScript(code).execute(ctx)
        tmp.forEach { ctx.put(it.first, it.second) }
        return ret
    }

    private val imported = HashMap<Pair<File, ContextType>, Context?>()

    private fun ctxFilePath(path: String): File {
        val ret = File(path).let { fs ->
            val fpath = (this.ctx.get("folder") as? String)
            if (!fs.isAbsolute && fpath != null) {
                val folder = File(fpath)
                folder.resolve(fs)
            } else {
                fs
            }
        }
        return ret
    }
    /**
     * loads once in separate context, if recursion occurs it is ignored
     */
    @JvmOverloads
    fun import(path: String, ctx: ContextType = (this.ctx.get("context") as Context).type, charset: String = Charset.defaultCharset().name()): Context {
        val fs = ctxFilePath(path)
        val access = Pair(fs, ctx)
        if (imported.containsKey(access))
            imported[access]?.let { return it }
        else
            imported.put(access, null)
        val file = this.ctx.put("file", fs.path)
        val folder = this.ctx.put("folder", fs.parentFile.path)
        try {
            val ret = readFile(fs.path, charset).build(Context(ctx))
            imported.put(access, ret)
            return ret
        } catch (t: Throwable) {
            System.err.println("error: $fs: $t")
            throw t
        } finally {
            this.ctx.put("file", file)
            this.ctx.put("folder", folder)
        }
    }

    /**
     * loads into specified context, useful for fragments
     */
    @JvmOverloads
    fun include(path: String, ctx: Context = this.ctx.get("context") as Context, charset: String = Charset.defaultCharset().name()): Context {
        val fs = ctxFilePath(path)
        val file = this.ctx.put("file", fs.path)
        val folder = this.ctx.put("folder", fs.parentFile.path)
        try {
            return readFile(fs.path, charset).build(ctx)
        } catch (t: Throwable) {
            System.err.println("error: $fs: $t")
            throw t
        } finally {
            this.ctx.put("file", file)
            this.ctx.put("folder", folder)
        }
    }

    fun data() = Data()
    fun tree() = NDefTree()
    fun node(features: List<Any>, filters: List<Any>) = NDefTree.NDefNode(features.toHashSet(), filters.toHashSet())
    fun genNode(features: List<Any>, filters: List<Any>, ordinal: Double, couldGen: Closure, gen: Closure) = object : NDefTree.NDefNodeGen(features.toHashSet(), filters.toHashSet(), ordinal) {
        override fun couldGen(elem: Any, features: List<Any>, filters: List<Any>): Boolean {
            return couldGen.execute(ctx, elem, features, filters) as Boolean? ?: false
        }

        override fun gen(elem: Any, features: List<Any>, filters: List<Any>): NDefTree.NDefNode? {
            return gen.execute(ctx, elem, features, filters) as? NDefTree.NDefNode
        }
    }

    fun nodeBuilder(fn: Closure): (Any, List<Any>, List<Any>) -> NDefTree.NDefNode? = { a, b, c -> fn.execute(ctx, a, b, c) as NDefTree.NDefNode? }

    @JvmOverloads
    fun pathOf(path: List<Any>, features: List<Any> = kotlin.collections.listOf(), filters: List<Any> = kotlin.collections.listOf()) = NDefPath(path, features, filters)


    /**
     * easy constructors for allowed types
     */
    fun listOf(vararg any: Any?) = listOf<Any?>(*any)

    fun arrayListOf(vararg any: Any?) = arrayListOf<Any?>(*any)
    fun mapOf(vararg any: Pair<Any?, Any?>) = mapOf<Any?, Any?>(*any)
    fun hashMapOf(vararg any: Pair<Any?, Any?>) = hashMapOf<Any?, Any?>(*any)
    fun pairOf(a: Any?, b: Any?) = Pair(a, b)
    fun heritableMapOf(vararg parents: HeritableMap<Any?, Any?>) = HeritableMap<Any?, Any?>()

    fun unescape(str: String): String {
        return StringEscapeUtils.unescapeJava(str)
    }
}

object Log {
    fun println(any: Any) = kotlin.io.println(any)
    fun tokenErr(token: Token, msg: Any) = "${token.location.origin}:${token.location.range}: token \"${token.text}\" error: $msg"
    fun `throw`(msg: String): Unit = throw RuntimeException(msg)
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

open class StringArtusReader(str: String, override val name: String) : ArtusReader {
    private var data = str
    private var offset = 0

    override fun build(ctx: Context): Context {
        var ctx: Context = ctx
        while (data.isNotEmpty()) {
            val stack = ctx.type.getStack().toList()
            ctx = stack.fold(null as Context?, { acc, contextMatcher ->
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
            }) ?: throw RuntimeException("no Context in source $name at $offset: \"${data.subSequence(0, min(80, data.lastIndex))}...\"")
        }
        return ctx
    }
}

val jexl = JexlBuilder().sandbox({
    val sandbox = JexlSandbox(false)
    Reflections("com.artuslang", SubTypesScanner(false)).getSubTypesOf(Object::class.java).forEach {
        sandbox.white(it.name)
    }
    sandbox.white(String::class.java.name)
    sandbox.white(Map::class.java.name)
    sandbox.white(List::class.java.name)
    sandbox.white(HashMap::class.java.name)
    sandbox.white(ArrayList::class.java.name)
    sandbox.white(HashSet::class.java.name)
    sandbox.white(Pair::class.java.name)
    Reflections("java.nio").getSubTypesOf(Buffer::class.java).forEach {
        sandbox.white(it.name)
    }
    sandbox
}()).create()

/**
 * use like ByteBuffer but flexible size, bytebuffers are based on capacity and not limit
 */
class Data {
    private val buf = arrayListOf<ByteGroup>()
    private var size = 0

    fun allocate(size: Int): ByteBuffer {
        val ret = ByteBuffer.allocate(size)
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

class ArrayByteGroup(override val dat: ByteBuffer, override val offset: Int) : ByteGroup {
    override val size = dat.capacity()
}

class NDefTree {

    val root = NDefNode(hashSetOf(), hashSetOf())

    open class NDefNode(val features: HashSet<Any>, val filters: HashSet<Any>) {
        protected val map = HashMap<Any, HashSet<NDefNode>>()
        protected val genMap = HashMap<Class<*>, HashSet<NDefNodeGen>>()
        val properties = HashMap<Any?, Any?>()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NDefNodeGen) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }


        fun get(elem: Any, features: List<Any>, filters: List<Any>): NDefNode? {
            var nodes: List<NDefNode> = map[elem]?.let { ArrayList(it) } ?: listOf()

            if (filters.isNotEmpty()) {
                nodes = nodes.filter { it.filters.containsAll(filters) }
            }
            if (features.isNotEmpty()) {
                nodes = nodes.filter { HashSet(features).containsAll(it.features) }
            }

            nodes = nodes.sortedWith(Comparator { o1, o2 ->
                val featureSize = o1.features.size - o2.features.size
                if (featureSize != 0) return@Comparator featureSize
                if (o1.features != o2.features) {
                    features.forEach {
                        val h1 = if (o1.features.contains(it)) 1 else 0
                        val h2 = if (o2.features.contains(it)) 1 else 0
                        val res = h1 - h2
                        if (res != 0) return@Comparator res
                    }
                }
                o1.filters.size - o2.filters.size
            })

            // generate if needed, first non null is valid
            return if (nodes.isEmpty()) {
                genMap[elem.javaClass]?.filter {
                    it.couldGen(elem, features, filters)
                }?.sortedBy {
                    it.ordinal
                }?.fold(null as NDefNode?, { acc, it ->
                    acc ?: it.gen(elem, features, filters)
                })
            } else
                nodes.firstOrNull()
        }

        fun put(elem: Any, node: NDefNode): NDefNode? {
            val nodes = map[elem]
            val nd = nodes?.find {
                it.features == node.features && it.filters == node.filters
            } ?: node
            if (nd.javaClass != node.javaClass) {
                return null
            }
            nd.features.addAll(features)
            nd.filters.addAll(filters)
            nodes?.add(nd) ?: map.getOrPut(elem, { hashSetOf() }).add(nd)
            return nd
        }

        fun put(classes: HashSet<Class<*>>, gen: NDefNodeGen) {
            classes.forEach {
                genMap.getOrPut(it, { hashSetOf() }).add(gen)
            }
        }

        fun getAllNodes(): HashSet<NDefNode> {
            return (map.values + genMap.values).flatten().toHashSet() // hashset because there can be duplicates on different classes (eg. a namespace can be bound to a string and ID)
        }

        /**
         * useful for buffering nodes, with different properties
         */
        fun copyWith(features: Array<Any>, filters: Array<Any>): NDefNode {
            val ret = NDefNode(features.toHashSet(), filters.toHashSet())
            ret.genMap.putAll(genMap)
            ret.map.putAll(map)
            ret.properties.putAll(properties)
            return ret
        }
    }

    abstract class NDefNodeGen(features: HashSet<Any>, filters: HashSet<Any>, val ordinal: Double) : NDefNode(features, filters) {

        /**
         * fast elimination check, false if cannot possibly generate, gen must not be called if return is false
         */
        abstract fun couldGen(elem: Any, features: List<Any>, filters: List<Any>): Boolean

        /**
         * attempt to generate, if fail return null, not buffered, it has to be handled by the implementation for speed
         */
        abstract fun gen(elem: Any, features: List<Any>, filters: List<Any>): NDefNode?
    }

    fun findNode(path: NDefPath): NDefNode? {
        return path.path.fold(root as NDefNode?, { acc, it ->
            acc?.get(it, path.features, path.filters)
        })
    }

    fun findNodeOrBuild(path: NDefPath, builders: HashMap<Class<*>, (Any, List<Any>, List<Any>) -> NDefNode?>): NDefNode? {
        return path.path.fold(root as NDefNode?, { acc, it ->
            if (acc == null) {
                return null
            }
            acc.get(it, path.features, path.filters) ?: builders[it.javaClass]?.invoke(it, path.features, path.filters)?.let { node -> acc.put(it, node) }
        })
    }
}

/**
 * [path] the path to follow nodes
 * [features] the path will priorize ambiguities with the most matching features, features defined with lower indexes have priority
 * [filters] the path will not follow any node that is missing a filter, if features are not discernable, it will take the one with closest filter match
 */
class NDefPath(val path: List<Any>, val features: List<Any> = listOf(), val filters: List<Any> = listOf())

class LayeredIterable<out T>(val base: Iterable<T>, val depth: Iterable<Iterable<T>>): Iterable<T> {

    override fun iterator(): Iterator<T> {
        return Iterators.concat(base.iterator(), Iterators.concat(depth.map { it.iterator() }.iterator()))
    }

}

class HeritableMap<T, U>(parents: List<HeritableMap<T, U>> = listOf()) {
    val parents = ArrayList<HeritableMap<T, U>>(parents)
    private val map = HashMap<T, U>()

    operator fun get(key: T): U? {
        var value = map[key]
        if (value == null) {
            parents.forEach {
                value = it[key]
                if (value != null)
                    return value
            }
        }
        return value
    }

    fun put(key: T, value: U): U? {
        return map.put(key, value)
    }
}