package org.jetbrains.kotlinconf.data

import libs.*
import kotlinx.cinterop.*
import kotlinx.serialization.json.*
import org.jetbrains.kotlinconf.model.*
import org.jetbrains.kotlinconf.util.*
import platform.Foundation.*

private val kHTTPStatusCodeComeBackLater = 477L
private val kHTTPStatusCodeTooLate = 478L

class KonfService(override val errorHandler: (NSError) -> Unit) : NetworkService {
    private companion object {
        private val OK_STATUS_CODES = listOf(kHTTPStatusCodeCreated, kHTTPStatusCodeOK)
        private val OK_WITH_TIME_STATUS_CODES = OK_STATUS_CODES + 
                listOf(kHTTPStatusCodeComeBackLater, kHTTPStatusCodeTooLate)
    }

    enum class VoteActionResult {
        OK, TOO_EARLY, TOO_LATE;

        companion object {
            fun fromCode(statusCode: Long) = when (statusCode) {
                kHTTPStatusCodeTooLate -> VoteActionResult.TOO_LATE
                kHTTPStatusCodeComeBackLater -> VoteActionResult.TOO_EARLY
                else -> VoteActionResult.OK
            }
        }
    }

    override val baseUrl = "https://api.kotlinconf.com"
    
    fun registerUser(uuid: String, onComplete: () -> Unit = {}) {
        val request = OMGHTTPURLRQ.POST(url("/users"), rawText = uuid)
        plainRequest(request, OK_STATUS_CODES + listOf(kHTTPStatusCodeConflict), { onComplete() })
    }

    fun getVotes(uuid: String, onComplete: (NSArray) -> Unit) {
        val request = OMGHTTPURLRQ.GET(url("/votes"), null, error = null)!!.acceptsJson().auth(uuid)
        jsonRequest(request) { onComplete(it.uncheckedCast<NSArray>()) }
    }

    fun addVote(session: Session, rating: SessionRating, uuid: String, onComplete: (VoteActionResult) -> Unit) {
        val request = OMGHTTPURLRQ.POST(url("/votes"),
            JSON = NSDictionary.dictionaryWithObjects(
                nsArrayOf(session.id!!.toNSString(), rating.value.toString().toNSString()),
                forKeys = nsArrayOf("sessionId".toNSString(), "rating".toNSString())
            ), 
        error = null)!!.acceptsJson().auth(uuid)
        
        plainRequest(request, OK_WITH_TIME_STATUS_CODES) { response ->
            onComplete(VoteActionResult.fromCode(response.statusCode))
        }
    }

    fun deleteVote(session: Session, uuid: String, onComplete: (VoteActionResult) -> Unit) {
        val request = OMGHTTPURLRQ.DELETE(url("/votes"), 
            JSON = NSDictionary.dictionaryWithObject(
                session.id!!.toNSString(),
                forKey = "sessionId".toNSString())
        ).acceptsJson().auth(uuid)

        plainRequest(request, OK_WITH_TIME_STATUS_CODES) { response ->
            onComplete(VoteActionResult.fromCode(response.statusCode))
        }
    }
 
    fun getFavorites(uuid: String, onComplete: (NSArray) -> Unit) {
        val request = OMGHTTPURLRQ.GET(url("/favorites"), null, error = null)!!.acceptsJson().auth(uuid)
        jsonRequest(request) { onComplete(it.uncheckedCast<NSArray>()) }
    }

    fun addFavorite(session: Session, uuid: String, onComplete: () -> Unit) {
        val request = OMGHTTPURLRQ.POST(url("/favorites"),
            JSON = session.toSessionIdJson(), error = null
        )!!.acceptsJson().auth(uuid)

        plainRequest(request, OK_STATUS_CODES, { onComplete() })
    }

    fun deleteFavorite(session: Session, uuid: String, onComplete: () -> Unit) {
        val request = OMGHTTPURLRQ.DELETE(url("/favorites"),
            JSON = session.toSessionIdJson()
        ).acceptsJson().auth(uuid)

        plainRequest(request, OK_STATUS_CODES, { onComplete() })
    }

    fun shouldShowBadge(onComplete: (Boolean) -> Unit) {
        val request = OMGHTTPURLRQ.GET(url("/keynote"), null, error = null)!!
        plainRequest(request, emptyList()) { response ->
            val showBadge = response.statusCode == kHTTPStatusCodeOK
            onComplete(showBadge)
        }
    }

    fun getSessions(uuid: String, onComplete: (AllData) -> Unit) {
//        jsonTypedRequest(
//                OMGHTTPURLRQ.GET(url("/all"), null, error = null)!!.acceptsJson().auth(uuid),
//                AllData.serializer(),
//                onComplete
//        )
        onComplete(JSON.Companion.parse(AllData.serializer(), fakeData))
    }
}

private fun Session.toSessionIdJson(): NSDictionary {
    return NSDictionary.dictionaryWithObject(this.id!!.toNSString(), forKey = "sessionId".toNSString())
}

private fun NSMutableURLRequest.acceptsJson(): NSMutableURLRequest {
    addValue("application/json", forHTTPHeaderField = "Accept")
    return this
}

private fun NSMutableURLRequest.textContentType(): NSMutableURLRequest {
    addValue("text/plain", forHTTPHeaderField = "Content-Type")
    return this
}

private fun NSMutableURLRequest.auth(uuid: String): NSMutableURLRequest {
    addValue("Bearer $uuid", forHTTPHeaderField = "Authorization")
    return this
}

private val fakeData = """{
	"sessions": [
		{
			"id": "12775",
			"isServiceSession": false,
			"isPlenumSession": true,
			"questionAnswers": [],
			"speakers": [
				"9671b9b6-771a-4df2-b800-1298c43b0a3b"
			],
			"description": "A nice collection of awesome things! ",
			"startsAt": "2017-11-02T09:00:00",
			"title": "Opening Keynote",
			"endsAt": "2017-11-02T10:00:00",
			"categoryItems": [
				1819
			],
			"roomId": 220
		}],
"rooms": [
		{
			"name": "St. Petersburg",
			"id": 220,
			"sort": 0
		},
		{
			"name": "Munich",
			"id": 221,
			"sort": 1
		}],
"speakers": null,
"questions": [],
"categories": [
		{
			"id": 459,
			"sort": 0,
			"title": "Level",
			"items": [
				{
					"name": "Intermediate",
					"id": 1820,
					"sort": 1
				},
				{
					"name": "Advanced",
					"id": 1821,
					"sort": 2
				},
				{
					"name": "Introductory and overview",
					"id": 1819,
					"sort": 3
				}
			]
		}
	],
"favorites": null,
"votes": null
}"""
