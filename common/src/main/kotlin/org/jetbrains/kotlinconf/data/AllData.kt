// Auto-generated file, do not modify!
package org.jetbrains.kotlinconf.data

import kotlin.Suppress
import kotlin.collections.List
import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerialClassDesc
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Optional
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.internal.NullableSerializer
import kotlinx.serialization.internal.SerialClassDescImplTagged

data class AllData(
        @Optional
        var sessions: List<Session>? = null,
        @Optional
        var rooms: List<Room>? = null,
        @Optional
        var speakers: List<Speaker>? = null,
        @Optional
        var questions: List<Question>? = null,
        @Optional
        var categories: List<Category>? = null,
        @Optional
        var favorites: List<Favorite>? = null,
        @Optional
        var votes: List<Vote>? = null
) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<AllData> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.AllData") {
            init {
                addElement("sessions")
                addElement("rooms")
                addElement("speakers")
                addElement("questions")
                addElement("categories")
                addElement("favorites")
                addElement("votes")
            }
        }

        override fun save(output: KOutput, obj: AllData) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(ArrayListSerializer(Session.serializer)), obj.sessions)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(ArrayListSerializer(Room.serializer)), obj.rooms)
            output.writeNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(ArrayListSerializer(Speaker.serializer)), obj.speakers)
            output.writeNullableSerializableElementValue(serialClassDesc, 3, NullableSerializer(ArrayListSerializer(Question.serializer)), obj.questions)
            output.writeNullableSerializableElementValue(serialClassDesc, 4, NullableSerializer(ArrayListSerializer(Category.serializer)), obj.categories)
            output.writeNullableSerializableElementValue(serialClassDesc, 5, NullableSerializer(ArrayListSerializer(Favorite.serializer)), obj.favorites)
            output.writeNullableSerializableElementValue(serialClassDesc, 6, NullableSerializer(ArrayListSerializer(Vote.serializer)), obj.votes)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): AllData {
            val input = input.readBegin(serialClassDesc)
            var local0: List<Session>? = null
            var local1: List<Room>? = null
            var local2: List<Speaker>? = null
            var local3: List<Question>? = null
            var local4: List<Category>? = null
            var local5: List<Favorite>? = null
            var local6: List<Vote>? = null
            var bitMask: Int = 0
            mainLoop@while (true) {
                val idx = input.readElement(serialClassDesc)
                when (idx) {
                    -1 -> {
                        break@mainLoop
                    }
                    0 -> {
                        local0 = input.readNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(ArrayListSerializer(Session.serializer)))
                        bitMask = bitMask or 1
                    }
                    1 -> {
                        local1 = input.readNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(ArrayListSerializer(Room.serializer)))
                        bitMask = bitMask or 2
                    }
                    2 -> {
                        local2 = input.readNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(ArrayListSerializer(Speaker.serializer)))
                        bitMask = bitMask or 4
                    }
                    3 -> {
                        local3 = input.readNullableSerializableElementValue(serialClassDesc, 3, NullableSerializer(ArrayListSerializer(Question.serializer)))
                        bitMask = bitMask or 8
                    }
                    4 -> {
                        local4 = input.readNullableSerializableElementValue(serialClassDesc, 4, NullableSerializer(ArrayListSerializer(Category.serializer)))
                        bitMask = bitMask or 16
                    }
                    5 -> {
                        local5 = input.readNullableSerializableElementValue(serialClassDesc, 5, NullableSerializer(ArrayListSerializer(Favorite.serializer)))
                        bitMask = bitMask or 32
                    }
                    6 -> {
                        local6 = input.readNullableSerializableElementValue(serialClassDesc, 6, NullableSerializer(ArrayListSerializer(Vote.serializer)))
                        bitMask = bitMask or 64
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
            if (bitMask and 4 == 0) {
                local2 = null
            }
            if (bitMask and 8 == 0) {
                local3 = null
            }
            if (bitMask and 16 == 0) {
                local4 = null
            }
            if (bitMask and 32 == 0) {
                local5 = null
            }
            if (bitMask and 64 == 0) {
                local6 = null
            }
            return AllData(local0, local1, local2, local3, local4, local5, local6)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}
