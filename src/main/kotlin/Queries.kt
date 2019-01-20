fun worstMatch(matches: List<Match>, playerName: String): Match {
    return matches.filter {
        val stats = it.playerStats[playerName]
        if(stats == null) false
        else !stats.isBot
    }.minBy {
        val stats = it.playerStats[playerName]
        stats?.pts ?: Int.MAX_VALUE
    }!!
}

fun worstMatchNoForfeit(matches: List<Match>, playerName: String, selector: (Stats) -> Int): Match {
    return matches.filter {
        if(it.finalScore0 < 16 && it.finalScore1 < 16) false
        else {
            val stats = it.playerStats[playerName]
            if (stats == null) false
            else !stats.isBot
        }
    }.minBy {
        selector.invoke(it.playerStats[playerName]!!)
    }!!
}

fun bestMatch(matches: List<Match>, playerName: String, selector: (Stats) -> Int): Match {
    return matches.filter {
        val stats = it.playerStats[playerName]
        if(stats == null) false
        else !stats.isBot
    }.maxBy {
        selector.invoke(it.playerStats[playerName]!!)
    }!!
}

fun mostPlayedPlayers(matches: List<Match>, take: Int = 50) {
    matches.map {
        it.playerStats.keys
    }.flatten().groupBy { it }.asSequence().sortedBy { -it.value.size }.take(take).forEach {
        println(it.key + ": " + it.value.size + ", ")
    }
}

fun mostPlayedMaps(matches: List<Match>, take: Int = 20) {
    matches.map {
        it.map
    }.groupBy { it }.asSequence().sortedBy { -it.value.size }.take(take).forEach {
        println(it.key + ": " + it.value.size + ", ")
    }
}

