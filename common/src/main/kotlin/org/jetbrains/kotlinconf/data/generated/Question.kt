// Auto-generated file, do not modify!
package org.jetbrains.kotlinconf.data.generated

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerialClassDesc
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.NullableSerializer
import kotlinx.serialization.internal.SerialClassDescImplTagged
import kotlinx.serialization.internal.StringSerializer

data class Question(
        val question: String?,
        val id: Int?,
        val sort: Int?,
        val questionType: String?
) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<Question> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.generated.Question") {
            init {
                addElement("question")
                addElement("id")
                addElement("sort")
                addElement("questionType")
            }
        }

        override fun save(output: KOutput, obj: Question) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(StringSerializer), obj.question)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(IntSerializer), obj.id)
            output.writeNullableSerializableElementValue(serialClassDesc, 2, NullableSerializer(IntSerializer), obj.sort)
            output.writeNullableSerializableElementValue(serialClassDesc, 3, NullableSerializer(StringSerializer), obj.questionType)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): Question {
            val input = input.readBegin(serialClassDesc)
            var local0: String? = null
            var local1: Int? = null
            var local2: Int? = null
            var local3: String? = null
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
                    3 -> {
                        local3 = input.readNullableSerializableElementValue(serialClassDesc, 3, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 8
                    }
                }
            }
            input.readEnd(serialClassDesc)
            if (bitMask and 1 == 0) {
                throw MissingFieldException("question")
            }
            if (bitMask and 2 == 0) {
                throw MissingFieldException("id")
            }
            if (bitMask and 4 == 0) {
                throw MissingFieldException("sort")
            }
            if (bitMask and 8 == 0) {
                throw MissingFieldException("questionType")
            }
            return Question(local0, local1, local2, local3)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}

data class QuestionAnswer(val questionId: Int?, val answerValue: String?) {
    @Suppress("NAME_SHADOWING")
    object serializer : KSerializer<QuestionAnswer> {
        override val serialClassDesc: KSerialClassDesc =
                object : SerialClassDescImplTagged("org.jetbrains.kotlinconf.data.generated.QuestionAnswer") {
            init {
                addElement("questionId")
                addElement("answerValue")
            }
        }

        override fun save(output: KOutput, obj: QuestionAnswer) {
            val output = output.writeBegin(serialClassDesc)
            output.writeNullableSerializableElementValue(serialClassDesc, 0, NullableSerializer(IntSerializer), obj.questionId)
            output.writeNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(StringSerializer), obj.answerValue)
            output.writeEnd(serialClassDesc)
        }

        override fun load(input: KInput): QuestionAnswer {
            val input = input.readBegin(serialClassDesc)
            var local0: Int? = null
            var local1: String? = null
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
                        local1 = input.readNullableSerializableElementValue(serialClassDesc, 1, NullableSerializer(StringSerializer))
                        bitMask = bitMask or 2
                    }
                }
            }
            input.readEnd(serialClassDesc)
            if (bitMask and 1 == 0) {
                throw MissingFieldException("questionId")
            }
            if (bitMask and 2 == 0) {
                throw MissingFieldException("answerValue")
            }
            return QuestionAnswer(local0, local1)
        }
    }
    companion object {
        fun serializer() = serializer
    }
}
