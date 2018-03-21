// Auto-generated file, do not modify!
package org.jetbrains.kotlinconf.data

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerialClassDesc
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Optional
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.NullableSerializer
import kotlinx.serialization.internal.SerialClassDescImplTagged
import kotlinx.serialization.internal.StringSerializer

data class Favorite(@Optional
var sessionId: String? = null) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<Favorite> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.Favorite") {
            init {
                addElement("sessionId")
            }
        }

        override fun save(output: KOutput, obj: Favorite) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(StringSerializer), obj.sessionId)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): Favorite {
            val input = input.readBegin(serialClassDesc)
            var local0: String? = null
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
                }
            }
            input.readEnd(serialClassDesc)
            if (bitMask and 1 == 0) {
                local0 = null
            }
            return Favorite(local0)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}

data class Link(
        val linkType: String?,
        val title: String?,
        val url: String?
) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<Link> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.Link") {
            init {
                addElement("linkType")
                addElement("title")
                addElement("url")
            }
        }

        override fun save(output: KOutput, obj: Link) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(StringSerializer), obj.linkType)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(StringSerializer), obj.title)
            output.writeNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(StringSerializer), obj.url)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): Link {
            val input = input.readBegin(serialClassDesc)
            var local0: String? = null
            var local1: String? = null
            var local2: String? = null
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
                        local1 = input.readNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 2
                    }
                    2 -> {
                        local2 = input.readNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 4
                    }
                }
            }
            input.readEnd(serialClassDesc)
            if (bitMask and 1 == 0) {
                throw MissingFieldException("linkType")
            }
            if (bitMask and 2 == 0) {
                throw MissingFieldException("title")
            }
            if (bitMask and 4 == 0) {
                throw MissingFieldException("url")
            }
            return Link(local0, local1, local2)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}

data class Room(
        val name: String?,
        val id: Int?,
        val sort: Int?
) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<Room> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.Room") {
            init {
                addElement("name")
                addElement("id")
                addElement("sort")
            }
        }

        override fun save(output: KOutput, obj: Room) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(StringSerializer), obj.name)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(IntSerializer), obj.id)
            output.writeNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(IntSerializer), obj.sort)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): Room {
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
            return Room(local0, local1, local2)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}

data class Vote(@Optional
var sessionId: String? = null, @Optional
var rating: Int? = null) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<Vote> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.Vote") {
            init {
                addElement("sessionId")
                addElement("rating")
            }
        }

        override fun save(output: KOutput, obj: Vote) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(StringSerializer), obj.sessionId)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(IntSerializer), obj.rating)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): Vote {
            val input = input.readBegin(serialClassDesc)
            var local0: String? = null
            var local1: Int? = null
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
                }
            }
            input.readEnd(serialClassDesc)
            if (bitMask and 1 == 0) {
                local0 = null
            }
            if (bitMask and 2 == 0) {
                local1 = null
            }
            return Vote(local0, local1)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}
