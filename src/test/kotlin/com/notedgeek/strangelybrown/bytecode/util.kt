package com.notedgeek.strangelybrown.bytecode

import java.io.*

class Util

fun getClassDataInput(pkg: String, name: String): DataInput {
    val qualifiedName = "$pkg/$name"
    val inputStream = Util::class.java.classLoader.getResourceAsStream("${qualifiedName}.class")
        ?: throw ClassNotFoundException(qualifiedName)
    return DataInputStream(inputStream)
}

internal class MyClassLoader internal constructor(parent: ClassLoader) : ClassLoader(parent) {
    fun getClassFromByte(name: String, bytecode: ByteArray): Class<*> {
        val c = defineClass(name.replace('/', '.'), bytecode, 0, bytecode.size)
        resolveClass(c)
        return c
    }
}

internal class RoundTripClassLoader(private val pkg: String) {
    fun loadClass(name: String): Class<*> {
        val dataInput = getClassDataInput(pkg, name)
        val clazz = loadClassFile(dataInput)
        val byteArrayOutputStream = ByteArrayOutputStream()
        writeClassfile(clazz, DataOutputStream(byteArrayOutputStream))
        val bytes = byteArrayOutputStream.toByteArray()
        loadClassFile(DataInputStream(ByteArrayInputStream(bytes)))
        return MyClassLoader(Util::class.java.classLoader).getClassFromByte(clazz.name, bytes)
    }
}

internal fun loadClassBuilderClass(byteCode: ByteArray): Class<*> {
    val dataInput = DataInputStream(ByteArrayInputStream(byteCode))
    val clazz = loadClassFile(dataInput)
    return MyClassLoader(Util::class.java.classLoader).getClassFromByte(clazz.name, byteCode)
}


