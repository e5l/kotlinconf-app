package org.jetbrains.kotlinconf.data

import kotlinx.serialization.*
import org.jetbrains.kotlinconf.util.*

@Serializable
data class Session(
        val id: String? = null,
        val isServiceSession: Boolean? = null,
        val isPlenumSession: Boolean? = null,
        val questionAnswers: List<QuestionAnswer?>? = null,
        val speakers: List<String?>? = null,
        val description: String? = null,
        @Serializable(with = CommonDateSerializer::class) val startsAt: Date? = null,
        val title: String? = null,
        @Serializable(with = CommonDateSerializer::class) val endsAt: Date? = null,
        val categoryItems: List<Int?>? = null,
        val roomId: Int? = null
)
