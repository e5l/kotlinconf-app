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
import org.jetbrains.kotlinconf.Date
import org.jetbrains.kotlinconf.serializer

data class Session(
        val id: String?,
        val isServiceSession: Boolean?,
        val isPlenumSession: Boolean?,
        val questionAnswers: List<QuestionAnswer?>?,
        val speakers: List<String?>?,
        val description: String?,
        val startsAt: Date?,
        val title: String?,
        val endsAt: Date?,
        val categoryItems: List<Int?>?,
        val roomId: Int?
) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<Session> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.Session") {
            init {
                addElement("id")
                addElement("isServiceSession")
                addElement("isPlenumSession")
                addElement("questionAnswers")
                addElement("speakers")
                addElement("description")
                addElement("startsAt")
                addElement("title")
                addElement("endsAt")
                addElement("categoryItems")
                addElement("roomId")
            }
        }

        override fun save(output: KOutput, obj: Session) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(StringSerializer), obj.id)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(BooleanSerializer), obj.isServiceSession)
            output.writeNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(BooleanSerializer), obj.isPlenumSession)
            output.writeNullableSerializableElementValue(serialClassDesc, 3, NullableSerializer(ArrayListSerializer(NullableSerializer(QuestionAnswer.serializer))), obj.questionAnswers)
            output.writeNullableSerializableElementValue(serialClassDesc, 4, NullableSerializer(ArrayListSerializer(NullableSerializer(StringSerializer))), obj.speakers)
            output.writeNullableSerializableElementValue(serialClassDesc, 5, NullableSerializer(StringSerializer), obj.description)
            output.writeNullableSerializableElementValue(serialClassDesc, 6, NullableSerializer(Date.serializer), obj.startsAt)
            output.writeNullableSerializableElementValue(serialClassDesc, 7, NullableSerializer(StringSerializer), obj.title)
            output.writeNullableSerializableElementValue(serialClassDesc, 8, NullableSerializer(Date.serializer), obj.endsAt)
            output.writeNullableSerializableElementValue(serialClassDesc, 9, NullableSerializer(ArrayListSerializer(NullableSerializer(IntSerializer))), obj.categoryItems)
            output.writeNullableSerializableElementValue(serialClassDesc, 10, NullableSerializer(IntSerializer), obj.roomId)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): Session {
            val input = input.readBegin(serialClassDesc)
            var local0: String? = null
            var local1: Boolean? = null
            var local2: Boolean? = null
            var local3: List<QuestionAnswer?>? = null
            var local4: List<String?>? = null
            var local5: String? = null
            var local6: Date? = null
            var local7: String? = null
            var local8: Date? = null
            var local9: List<Int?>? = null
            var local10: Int? = null
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
                        local1 = input.readNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(BooleanSerializer))
                        bitMask = bitMask or 2
                    }
                    2 -> {
                        local2 = input.readNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(BooleanSerializer))
                        bitMask = bitMask or 4
                    }
                    3 -> {
                        local3 = input.readNullableSerializableElementValue(serialClassDesc, 3, NullableSerializer(ArrayListSerializer(NullableSerializer(QuestionAnswer.serializer))))
                        bitMask = bitMask or 8
                    }
                    4 -> {
                        local4 = input.readNullableSerializableElementValue(serialClassDesc, 4, NullableSerializer(ArrayListSerializer(NullableSerializer(StringSerializer))))
                        bitMask = bitMask or 16
                    }
                    5 -> {
                        local5 = input.readNullableSerializableElementValue(serialClassDesc, 5, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 32
                    }
                    6 -> {
                        local6 = input.readNullableSerializableElementValue(serialClassDesc, 6, NullableSerializer(Date.serializer))
                        bitMask = bitMask or 64
                    }
                    7 -> {
                        local7 = input.readNullableSerializableElementValue(serialClassDesc, 7, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 128
                    }
                    8 -> {
                        local8 = input.readNullableSerializableElementValue(serialClassDesc, 8, NullableSerializer(Date.serializer))
                        bitMask = bitMask or 256
                    }
                    9 -> {
                        local9 = input.readNullableSerializableElementValue(serialClassDesc, 9, NullableSerializer(ArrayListSerializer(NullableSerializer(IntSerializer))))
                        bitMask = bitMask or 512
                    }
                    10 -> {
                        local10 = input.readNullableSerializableElementValue(serialClassDesc, 10, NullableSerializer(IntSerializer))
                        bitMask = bitMask or 1024
                    }
                }
            }
            input.readEnd(serialClassDesc)
            if (bitMask and 1 == 0) {
                throw MissingFieldException("id")
            }
            if (bitMask and 2 == 0) {
                throw MissingFieldException("isServiceSession")
            }
            if (bitMask and 4 == 0) {
                throw MissingFieldException("isPlenumSession")
            }
            if (bitMask and 8 == 0) {
                throw MissingFieldException("questionAnswers")
            }
            if (bitMask and 16 == 0) {
                throw MissingFieldException("speakers")
            }
            if (bitMask and 32 == 0) {
                throw MissingFieldException("description")
            }
            if (bitMask and 64 == 0) {
                throw MissingFieldException("startsAt")
            }
            if (bitMask and 128 == 0) {
                throw MissingFieldException("title")
            }
            if (bitMask and 256 == 0) {
                throw MissingFieldException("endsAt")
            }
            if (bitMask and 512 == 0) {
                throw MissingFieldException("categoryItems")
            }
            if (bitMask and 1024 == 0) {
                throw MissingFieldException("roomId")
            }
            return Session(local0, local1, local2, local3, local4, local5, local6, local7, local8, local9, local10)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}

data class SessionGroup(
        val groupName: String?,
        val sessions: List<Session?>?,
        val groupId: Int?
) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<SessionGroup> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.SessionGroup") {
            init {
                addElement("groupName")
                addElement("sessions")
                addElement("groupId")
            }
        }

        override fun save(output: KOutput, obj: SessionGroup) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(StringSerializer), obj.groupName)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(ArrayListSerializer(NullableSerializer(Session.serializer))), obj.sessions)
            output.writeNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(IntSerializer), obj.groupId)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): SessionGroup {
            val input = input.readBegin(serialClassDesc)
            var local0: String? = null
            var local1: List<Session?>? = null
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
                        local1 = input.readNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(ArrayListSerializer(NullableSerializer(Session.serializer))))
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
                throw MissingFieldException("groupName")
            }
            if (bitMask and 2 == 0) {
                throw MissingFieldException("sessions")
            }
            if (bitMask and 4 == 0) {
                throw MissingFieldException("groupId")
            }
            return SessionGroup(local0, local1, local2)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}
