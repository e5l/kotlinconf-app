// Auto-generated file, do not modify!
package org.jetbrains.kotlinconf.data.generated

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerialClassDesc
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.NullableSerializer
import kotlinx.serialization.internal.SerialClassDescImplTagged
import kotlinx.serialization.internal.StringSerializer

data class CategoryItem(
        val name: String?,
        val id: Int?,
        val sort: Int?
) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<CategoryItem> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.generated.CategoryItem") {
            init {
                addElement("name")
                addElement("id")
                addElement("sort")
            }
        }

        override fun save(output: KOutput, obj: CategoryItem) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(StringSerializer), obj.name)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(IntSerializer), obj.id)
            output.writeNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(IntSerializer), obj.sort)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): CategoryItem {
            val input = input.readBegin(serialClassDesc)
            var local0: String? = null
            var local1: Int? = null
            var local2: Int? = null
            var bitMask: Int = 0
            mainLoop@while (true) {
                val idx = input.readElement(serialClassDesc)
                when (idx) {
                    -1 -> {
                        break@mainLoop
                    }
                    0 -> {
                        local0 = input.readNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 1
                    }
                    1 -> {
                        local1 = input.readNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(IntSerializer))
                        bitMask = bitMask or 2
                    }
                    2 -> {
                        local2 = input.readNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(IntSerializer))
                        bitMask = bitMask or 4
                    }
                }
            }
            input.readEnd(serialClassDesc)
            if (bitMask and 1 == 0) {
                throw MissingFieldException("name")
            }
            if (bitMask and 2 == 0) {
                throw MissingFieldException("id")
            }
            if (bitMask and 4 == 0) {
                throw MissingFieldException("sort")
            }
            return CategoryItem(local0, local1, local2)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}

data class Category(
        val id: Int?,
        val sort: Int?,
        val title: String?,
        val items: List<CategoryItem?>?
) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<Category> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.generated.Category") {
            init {
                addElement("id")
                addElement("sort")
                addElement("title")
                addElement("items")
            }
        }

        override fun save(output: KOutput, obj: Category) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(IntSerializer), obj.id)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(IntSerializer), obj.sort)
            output.writeNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(StringSerializer), obj.title)
            output.writeNullableSerializableElementValue(serialClassDesc, 3, NullableSerializer(ArrayListSerializer(NullableSerializer(CategoryItem.serializer))), obj.items)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): Category {
            val input = input.readBegin(serialClassDesc)
            var local0: Int? = null
            var local1: Int? = null
            var local2: String? = null
            var local3: List<CategoryItem?>? = null
            var bitMask: Int = 0
            mainLoop@while (true) {
                val idx = input.readElement(serialClassDesc)
                when (idx) {
                    -1 -> {
                        break@mainLoop
                    }
                    0 -> {
                        local0 = input.readNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(IntSerializer))
                        bitMask = bitMask or 1
                    }
                    1 -> {
                        local1 = input.readNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(IntSerializer))
                        bitMask = bitMask or 2
                    }
                    2 -> {
                        local2 = input.readNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 4
                    }
                    3 -> {
                        local3 = input.readNullableSerializableElementValue(serialClassDesc, 3, NullableSerializer(ArrayListSerializer(NullableSerializer(CategoryItem.serializer))))
                        bitMask = bitMask or 8
                    }
                }
            }
            input.readEnd(serialClassDesc)
            if (bitMask and 1 == 0) {
                throw MissingFieldException("id")
            }
            if (bitMask and 2 == 0) {
                throw MissingFieldException("sort")
            }
            if (bitMask and 4 == 0) {
                throw MissingFieldException("title")
            }
            if (bitMask and 8 == 0) {
                throw MissingFieldException("items")
            }
            return Category(local0, local1, local2, local3)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}
