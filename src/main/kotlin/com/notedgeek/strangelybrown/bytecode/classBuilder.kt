package com.notedgeek.strangelybrown.bytecode

import com.notedgeek.strangelybrown.bytecode.attribute.Attribute
import java.io.ByteArrayOutputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.util.*

fun buildClass(classBuilder: ClassBuilder = ClassBuilder(), block: ClassBuilder.() -> Unit) = classBuilder.apply(block)

fun bytecodeFor(classBuilder: ClassBuilder = ClassBuilder(), block: ClassBuilder.() -> Unit = {}): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    classBuilder.apply(block).write(DataOutputStream(byteArrayOutputStream))
    return byteArrayOutputStream.toByteArray()
}

@DslMarker
annotation class ScopeMarker

@ScopeMarker
class ClassBuilder {

    private var minorVersion = 0
    private var majorVersion = 52
    private val constantPool = ConstantPool()
    private var name = "packageParent/packageChild"
    private var superclassName = "java/lang/Object"
    private var accessFlags = ACC_PUBLIC
    private val interfaceNames = LinkedList<String>()
    private val fields = LinkedList<Field>()
    private val methods = LinkedList<Method>()
    private val attributes = HashMap<String, Attribute>()
    private var addDefaultConstructor = true


    fun write(dataOutput: DataOutput) {
        writeClassfile(toClass(), dataOutput)
    }

    fun name(name: String) {
        this.name = name
    }

    fun superclassName(superclassName: String) {
        this.superclassName = superclassName

    }

    fun access(accessFlags: Int) {
        this.accessFlags = accessFlags
    }

    fun implements(vararg interfaceNames: String) {
        for (interfaceName in interfaceNames) {
            constantPool.ensureClass(interfaceName)
            this.interfaceNames.add(interfaceName)
        }
    }

    fun method(block: MethodBuilder.() -> Unit) {
        val method = buildMethod(constantPool, block)
        if (method.name == "<init>") {
            addDefaultConstructor = false
        }
        methods.add(method)
    }

    private fun toClass(): Clazz {
        constantPool.ensureClass(name)
        constantPool.ensureClass(superclassName)
        addDefaultConstructorIfNecessary()
        return Clazz(
            minorVersion,
            majorVersion,
            constantPool,
            accessFlags,
            name,
            superclassName,
            interfaceNames,
            fields,
            methods,
            attributes
        )
    }

    private fun addDefaultConstructorIfNecessary() {
        if (addDefaultConstructor) {
            methods.addFirst(
                buildMethod(constantPool) {
                    name("<init>")
                    descriptor("()V")
                    access(ACC_PUBLIC)
                    code {
                        aLoad(0)
                        invokeSpecial("java/lang/Object", "<init>", "()V")
                        rtn()
                    }
                }
            )
        }
    }
}
