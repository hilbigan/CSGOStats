import org.jsoup.Jsoup
import java.io.File
import java.lang.Exception
import java.util.*

typealias PlayerStats = MutableMap<String, Stats>

fun main(args: Array<String>) {
    if(args.size < 2) {
        println("Not enough arguments.\nUsage: csgostats.jar <file-path> <steam-name>")
        System.exit(1)
    }

    val f = File(args[0])
    val s = f.readText()
    println("Parsing file ...")
    val doc = Jsoup.parse(s)
    println("Done parsing.")

    // Read matches...

    val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
    val excludeIndices = listOf(0, 6)

    println("Reading matches ...")
    val matches: List<Match> = doc.getElementById("personaldata_elements_container").child(0).children().map { it.children() }.flatten().drop(1).mapIndexed { i, it ->
        try {
            val left = it.child(0).getElementsByClass("csgo_scoreboard_inner_left")[0].child(0)
            val mapText = left.child(0).text()
            val map = if (mapText.startsWith("Wettkampf")) mapText.substring(10) else mapText
            val dateText = left.child(1).text()
            val dateFields = dateText.split("-", ":", " ").dropLast(1).map(String::toInt)
            cal.set(dateFields[0], dateFields[1] - 1, dateFields[2], dateFields[3], dateFields[4], dateFields[5])

            val date = cal.time
            val queueTime = stringToSeconds(left.child(2).text())
            val playTime = stringToSeconds(left.child(3).text())

            val playerStats: PlayerStats = hashMapOf()
            val right = it.child(1).child(0).child(0)
            right.children().filterIndexed { index, _ -> index !in excludeIndices }.forEachIndexed { i, it ->
                val name = it.getElementsByClass("playerNickname ellipsis")[0].text()
                val ping: Int = it.child(1).text().toInt()
                val kills: Int = it.child(2).text().toInt()
                val assists: Int = it.child(3).text().toInt()
                val deaths: Int = it.child(4).text().toInt()
                val mvpText = it.child(5).text()
                val mvps =
                    if (mvpText.isBlank()) 0 else if (mvpText.length == 1) 1 else mvpText.filter(Char::isDigit).toInt()
                val hsText = it.child(6).text().filter(Char::isDigit)
                val headshots: Int = if (hsText.isBlank()) 0 else it.child(6).text().filter(Char::isDigit).toInt()
                val pts: Int = it.child(7).text().toInt()

                playerStats[name] = Stats(ping, kills, assists, deaths, mvps, headshots, pts, ping == 0, team = if (i < 5) 0 else 1)
            }

            val finalScoreText = right.children()[6].child(0).text().split(":")
            val finalScore0 = finalScoreText[0].trim().toInt()
            val finalScore1 = finalScoreText[1].trim().toInt()

            Match(map, date, queueTime, playTime, playerStats, finalScore0, finalScore1)
        } catch (e: Exception){
            println("Encountered an error, skipping one match.")
            null
        }
    }.filterNotNull().toList()
    println("Done reading matches: n = ${matches.size}")

    // Print results

    println("\n**Results:**  \n")
    println("## Worst match by points:  \n")
    worstMatch(matches, args[1]).prettyPrint()

    println("\n## Worst match by points (no forfeits):  \n")
    worstMatchNoForfeit(matches, args[1]) { it.pts }.prettyPrint(highlight = args[1])

    println("\n## Worst match by kills (no forfeits):  \n")
    worstMatchNoForfeit(matches, args[1]) { it.kills }.prettyPrint(highlight = args[1])

    println("\n## Worst match by K/D (no forfeits):  \n")
    worstMatchNoForfeit(matches, args[1]) { (it.kd * 100000).toInt() }.prettyPrint(highlight = args[1])

    println("\n## Best match by points:  \n")
    bestMatch(matches, args[1]) { it.pts }.prettyPrint(highlight = args[1])

    println("\n## Best match by kills:  \n")
    bestMatch(matches, args[1]) { it.kills }.prettyPrint(highlight = args[1])

    println("\n## Best match by K/D:  \n")
    bestMatch(matches, args[1]) { (it.kd * 100000).toInt() }.prettyPrint(highlight = args[1])

    println("\n## Most frequently played maps (Top 20):  \n")
    mostPlayedMaps(matches)

    println("\n## Most frequent teammates and enemies (Top 50):  \n")
    mostPlayedPlayers(matches)

    println("\n## Longest Streaks: \n")

    fun printStreak(list: List<Match>){
        list.forEach {
            if(it.playerStats[args[1]]!!.team == 0){
                print("${it.finalScore0}:${it.finalScore1}, ")
            } else {
                print("${it.finalScore1}:${it.finalScore0}, ")
            }
        }
        println()
    }

    val wins = longestStreak(matches) { match -> match.isWon(args[1])}
    val looses = longestStreak(matches) { match -> !match.isWon(args[1]) && !match.isTied()}
    println("Longest *winstreak*: ${wins.first().date} - ${wins.last().date}, ${wins.size} matches")
    printStreak(wins)
    println("Longest *loosestreak*: ${looses.first().date} - ${looses.last().date}, ${looses.size} matches")
    printStreak(looses)

    val winsAndTies = longestStreak(matches) { match -> match.isWon(args[1]) || match.isTied()}
    val loosesAndTies = longestStreak(matches) { match -> !match.isWon(args[1])}
    val ties = longestStreak(matches) { match -> match.isTied()}
    println("Longest *winstreak* with ties: ${winsAndTies.first().date} - ${winsAndTies.last().date}, ${winsAndTies.size} matches (${winsAndTies.filter { it.isWon(args[1]) }.size} wins)")
    printStreak(winsAndTies)
    println("Longest *loosestreak* with ties: ${loosesAndTies.first().date} - ${loosesAndTies.last().date}, ${loosesAndTies.size} matches (${loosesAndTies.filter { !it.isWon(args[1]) && !it.isTied() }.size} looses)")
    printStreak(loosesAndTies)

    println("Longest *draw* streak: ${ties.first().date} - ${ties.last().date}, ${ties.size} matches")
    printStreak(ties)



    println("\n## Other stats\n")
    for (i in 100..130){
        val mts = matches.filter { it.date.year == i }
        if(mts.count() == 0){
            continue
        }

        print("Matches played in ${1900 + i}: ${mts.count()} - Top 3 Maps: ")
        mts.map { it.map }.groupBy { it }.asSequence().sortedBy { -it.value.size }.take(3).forEach {
            print(it.key + " (" + it.value.size + " matches) ")
        }
        println("  ")
    }

    println("\n(This output is in markdown format. You can convert this to pdf online - search for \"markdown to pdf\" - to get a more structured view.)")
}

fun stringToSeconds(str: String, drop: Int = 1): Int {
    var base = 1
    var result = 0
    str.split(":").drop(drop).map(String::trim).reversed().forEach {
        result += it.toInt() * base
        base *= 60
    }
    return result
}
