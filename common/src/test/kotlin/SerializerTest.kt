package org.jetbrains.kotlinconf

import kotlinx.serialization.json.JSON
import org.jetbrains.kotlinconf.data.AllData
import org.jetbrains.kotlinconf.data.Room
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SerializerTest {

    private val jsonInput = """{
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

    @Test
    fun canParseAll() {
        val allData = JSON.parse(AllData.serializer(), jsonInput)
        with(allData) {
            assertEquals(null, votes)
            assertEquals(null, favorites)
            assertEquals(null, speakers)
            assertEquals(emptyList(), questions)
            assertEquals(listOf(
                Room("St. Petersburg", 220, 0),
                Room("Munich", 221, 1)
            ), rooms)
            assertNotNull(sessions)
            assertEquals(1, sessions!!.size)
            val date = sessions!!.first().startsAt
            assertNotNull(date)
            with(date!!) {
                println(this.toReadableDateTimeString())
                assertEquals(2017, getFullYear())
                assertEquals(10, getMonth())
                assertEquals(2, getDate())
                assertEquals(9, getHours())
            }
        }
    }
}
