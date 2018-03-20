import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asTypeName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import kotlinx.serialization.sourcegen.*
import java.io.File
import kotlin.reflect.KClass

private val <T : Any> KClass<T>.nullableRef: ClassName
    get() = this.asTypeName().asNullable()

private fun ClassName.listify() = ParameterizedTypeName.get(List::class.asTypeName(), this).asNullable()

class Generator(val outputDir: File) {
    val nullI = Int::class.nullableRef
    val nullS = String::class.nullableRef
    val pkg = "org.jetbrains.kotlinconf.data.generated"

    fun execute() {
        genCategory()
        genMisc()
        genSpeaker()
        genQuestions()
        genSession()
        genAll()
    }

    private fun genCategory() {
        saveFile(outputDir, pkg, "Category") {
            val item = serializableClass("CategoryItem") {
                property("name", nullS)
                property("id", nullI)
                property("sort", nullI)
            }.serializableClassName
            category = serializableClass("Category") {
                property("id", nullI)
                property("sort", nullI)
                property("title", nullS)
                property("items", item.asNullable().listify())
            }.serializableClassName
        }
    }

    private lateinit var linkRef: ClassName
    private lateinit var qaRef: ClassName

    private lateinit var session: ClassName
    private lateinit var room: ClassName
    private lateinit var speaker: ClassName
    private lateinit var qRef: ClassName
    private lateinit var category: ClassName
    private lateinit var fav: ClassName
    private lateinit var vote: ClassName

    private fun genMisc() {
        saveFile(outputDir, pkg, "Misc") {
            fav = serializableClass("Favorite") {
                property("sessionId", nullS)
            }.serializableClassName

            linkRef = serializableClass("Link") {
                property("linkType", nullS)
                property("title", nullS)
                property("url", nullS)
            }.serializableClassName

            room = serializableClass("Room") {
                property("name", nullS)
                property("id", nullI)
                property("sort", nullI)
            }.serializableClassName

            vote = serializableClass("Vote") {
                property("sessionId", nullS)
                property("rating", nullI)
            }.serializableClassName
        }
    }

    private fun genSpeaker() {
        saveFile(outputDir, pkg, "Speaker") {
            speaker = serializableClass("Speaker") {
                property("firstName", nullS)
                property("lastName", nullS)
                property("profilePicture", nullS)
                property("sessions", nullI.listify())
                property("tagLine", nullS)
                property("isTopSpeaker", Boolean::class.nullableRef)
                property("bio", nullS)
                property("fullName", nullS)
                property("links", linkRef.asNullable().listify())
                property("id", nullS)
            }.serializableClassName
        }
    }

    private fun genQuestions() {
        saveFile(outputDir, pkg, "Question") {
            qRef= serializableClass("Question") {
                property("question", nullS)
                property("id", nullI)
                property("sort", nullI)
                property("questionType", nullS)
            }.serializableClassName

            qaRef = serializableClass("QuestionAnswer") {
                property("questionId", nullI)
                property("answerValue", nullS)
            }.serializableClassName
        }
    }

    private fun genSession() {

        val dateRef = ClassName("org.jetbrains.kotlinconf", "Date")

        saveFile(outputDir, pkg, "Session") {
            session = serializableClass("Session") {
                property("id", nullS)
                property("isServiceSession", Boolean::class.nullableRef)
                property("isPlenumSession", Boolean::class.nullableRef)
                property("questionAnswers", qaRef.asNullable().listify())
                property("speakers", nullS.listify())
                property("description", nullS)
                property("startsAt", dateRef.asNullable())
                property("title", nullS)
                property("endsAt", dateRef.asNullable())
                property("categoryItems", nullI.listify())
                property("roomId", nullI)
            }.serializableClassName

            serializableClass("SessionGroup") {
                property("groupName", nullS)
                property("sessions", session.asNullable().listify())
                property("groupId", nullI)
            }
        }
    }

    private fun genAll() {
        saveFile(outputDir, pkg, "AllData") {
            serializableClass("AllData") {
                property("sessions", session.listify())
                property("rooms", room.listify())
                property("speakers", speaker.listify())
                property("questions", qRef.listify())
                property("categories", category.listify())
                property("favorites", fav.listify())
                property("votes", vote.listify())
            }
        }
    }

}

open class DataClassesGenerator: DefaultTask() {
    var outputDir: File = File(project.projectDir, "src/main/kotlin")

    @TaskAction
    fun run() {
        Generator(outputDir).execute()
    }
}
