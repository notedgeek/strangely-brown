package com.notedgeek.strangelybrown.bytecode.builder

import com.notedgeek.strangelybrown.bytecode.*
import com.notedgeek.strangelybrown.bytecode.attribute.Attribute
import java.io.DataOutput
import java.util.*

fun buildClass(classBuilder: ClassBuilder = ClassBuilder(), block: ClassBuilder.() -> Unit) = classBuilder.apply(block)

fun writeClass(dataOutput: DataOutput, classBuilder: ClassBuilder = ClassBuilder(), block: ClassBuilder.() -> Unit) =
    classBuilder.apply(block).write(dataOutput)

class ClassBuilder() {

    internal var name = "package/ClassName"
    private var minorVersion = 0
    private var majorVersion = 52
    private val constantPool = ConstantPool()
    private var superclassName = "java/lang/Object"
    private var accessFlags = 0
    private val interfaceNames = LinkedList<String>()
    private val fields = LinkedList<Field>()
    private val methods = LinkedList<Method>()
    private val attributes = HashMap<String, Attribute>()

    private fun toClass(): Clazz {
        constantPool.ensureClass(name)
        constantPool.ensureClass(superclassName)
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

    fun write(dataOutput: DataOutput) {
        writeClassfile(toClass(), dataOutput)
    }

    fun name(name: String) {
        this.name = name.fromDotted()
    }

    fun superclassName(superclassName: String) {
        this.superclassName = superclassName.fromDotted()
    }

    fun implements(vararg interfaceNames: String) {
        for (interfaceName in interfaceNames.map(String::fromDotted)) {
            constantPool.ensureClass(interfaceName)
            this.interfaceNames.add(interfaceName)
        }
    }
}

private fun String.fromDotted() = this.replace('.', '/')