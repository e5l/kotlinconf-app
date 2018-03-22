package org.jetbrains.kotlinconf.data

import org.jetbrains.kotlinconf.*

object AppContext {
    var allData: AllData? = null

    var localFavorites: MutableList<Favorite> = mutableListOf()
    var localVotes: MutableList<Vote> = mutableListOf()

    var sessionsModels: List<SessionModel>? = null
}
