import java.util.*

data class Match(val map: String, val date: Date, val queueTime: Int, val playTime: Int, val playerStats: PlayerStats, val finalScore0: Int, val finalScore1: Int){

    fun prettyPrint(highlight: String = ""){
        println("*$finalScore0:$finalScore1* - **Map: $map, Date: $date, queueTime=${queueTime}s, playTime=${playTime}s**\n")
        println("**Name** | **Ping** | **K** | **A** | **D** | **MVP** | **HS%** | **K/D** | **PTS** | **Team**")
        println(":-|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|-:")
        orderedPlayerStats().keys.forEachIndexed { i, it ->
            if(i == 5) println("**$finalScore0** : **$finalScore1** | --- | --- | --- | --- | --- | --- | --- | --- | ---")
            if(highlight == it){
                println("**$it** | ${playerStats[it]}")
            } else {
                println("$it | ${playerStats[it]}")
            }
        }
    }

    private fun orderedPlayerStats(): PlayerStats {
        val stats: PlayerStats = mutableMapOf()
        playerStats.keys.sortedWith(kotlin.Comparator { o1, o2 ->
            (playerStats[o1]!!.team - playerStats[o2]!!.team) * 1000 + playerStats[o2]!!.pts - playerStats[o1]!!.pts
        }).forEach {
            stats[it] = playerStats[it]!!
        }
        return stats
    }

    fun isWon(playerName: String): Boolean {
        return when (playerStats[playerName]!!.team){
            0 -> finalScore0 > finalScore1
            1 -> finalScore0 < finalScore1
            else -> false
        }
    }

    fun isTied(): Boolean {
        return finalScore1 == finalScore0
    }

}