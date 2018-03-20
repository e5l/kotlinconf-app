// Auto-generated file, do not modify!
package org.jetbrains.kotlinconf.data

import kotlin.Boolean
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
import kotlinx.serialization.internal.BooleanSerializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.NullableSerializer
import kotlinx.serialization.internal.SerialClassDescImplTagged
import kotlinx.serialization.internal.StringSerializer

data class Speaker(
        val firstName: String?,
        val lastName: String?,
        val profilePicture: String?,
        val sessions: List<Int?>?,
        val tagLine: String?,
        val isTopSpeaker: Boolean?,
        val bio: String?,
        val fullName: String?,
        val links: List<Link?>?,
        val id: String?
) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<Speaker> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.Speaker") {
            init {
                addElement("firstName")
                addElement("lastName")
                addElement("profilePicture")
                addElement("sessions")
                addElement("tagLine")
                addElement("isTopSpeaker")
                addElement("bio")
                addElement("fullName")
                addElement("links")
                addElement("id")
            }
        }

        override fun save(output: KOutput, obj: Speaker) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(StringSerializer), obj.firstName)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(StringSerializer), obj.lastName)
            output.writeNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(StringSerializer), obj.profilePicture)
            output.writeNullableSerializableElementValue(serialClassDesc, 3, NullableSerializer(ArrayListSerializer(NullableSerializer(IntSerializer))), obj.sessions)
            output.writeNullableSerializableElementValue(serialClassDesc, 4, NullableSerializer(StringSerializer), obj.tagLine)
            output.writeNullableSerializableElementValue(serialClassDesc, 5, NullableSerializer(BooleanSerializer), obj.isTopSpeaker)
            output.writeNullableSerializableElementValue(serialClassDesc, 6, NullableSerializer(StringSerializer), obj.bio)
            output.writeNullableSerializableElementValue(serialClassDesc, 7, NullableSerializer(StringSerializer), obj.fullName)
            output.writeNullableSerializableElementValue(serialClassDesc, 8, NullableSerializer(ArrayListSerializer(NullableSerializer(Link.serializer))), obj.links)
            output.writeNullableSerializableElementValue(serialClassDesc, 9, NullableSerializer(StringSerializer), obj.id)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): Speaker {
            val input = input.readBegin(serialClassDesc)
            var local0: String? = null
            var local1: String? = null
            var local2: String? = null
            var local3: List<Int?>? = null
            var local4: String? = null
            var local5: Boolean? = null
            var local6: String? = null
            var local7: String? = null
            var local8: List<Link?>? = null
            var local9: String? = null
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
                    3 -> {
                        local3 = input.readNullableSerializableElementValue(serialClassDesc, 3, NullableSerializer(ArrayListSerializer(NullableSerializer(IntSerializer))))
                        bitMask = bitMask or 8
                    }
                    4 -> {
                        local4 = input.readNullableSerializableElementValue(serialClassDesc, 4, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 16
                    }
                    5 -> {
                        local5 = input.readNullableSerializableElementValue(serialClassDesc, 5, NullableSerializer(BooleanSerializer))
                        bitMask = bitMask or 32
                    }
                    6 -> {
                        local6 = input.readNullableSerializableElementValue(serialClassDesc, 6, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 64
                    }
                    7 -> {
                        local7 = input.readNullableSerializableElementValue(serialClassDesc, 7, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 128
                    }
                    8 -> {
                        local8 = input.readNullableSerializableElementValue(serialClassDesc, 8, NullableSerializer(ArrayListSerializer(NullableSerializer(Link.serializer))))
                        bitMask = bitMask or 256
                    }
                    9 -> {
                        local9 = input.readNullableSerializableElementValue(serialClassDesc, 9, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 512
                    }
                }
            }
            input.readEnd(serialClassDesc)
            if (bitMask and 1 == 0) {
                throw MissingFieldException("firstName")
            }
            if (bitMask and 2 == 0) {
                throw MissingFieldException("lastName")
            }
            if (bitMask and 4 == 0) {
                throw MissingFieldException("profilePicture")
            }
            if (bitMask and 8 == 0) {
                throw MissingFieldException("sessions")
            }
            if (bitMask and 16 == 0) {
                throw MissingFieldException("tagLine")
            }
            if (bitMask and 32 == 0) {
                throw MissingFieldException("isTopSpeaker")
            }
            if (bitMask and 64 == 0) {
                throw MissingFieldException("bio")
            }
            if (bitMask and 128 == 0) {
                throw MissingFieldException("fullName")
            }
            if (bitMask and 256 == 0) {
                throw MissingFieldException("links")
            }
            if (bitMask and 512 == 0) {
                throw MissingFieldException("id")
            }
            return Speaker(local0, local1, local2, local3, local4, local5, local6, local7, local8, local9)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}
