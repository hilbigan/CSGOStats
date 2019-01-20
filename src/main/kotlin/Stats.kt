class Stats(val ping: Int, val kills: Int, val assists: Int, val deaths: Int, val mvp: Int, val headshots: Int, val pts: Int, val isBot: Boolean, val team: Int){
    val kd: Float = kills / (if(deaths > 0) deaths.toFloat() else 1f)

    override fun toString(): String {
        return "${if(isBot) "BOT" else "${ping}ms"} | $kills | $assists | $deaths | $mvp★ | $headshots% | ${java.lang.String.format("%.2f", kd)} | $pts | $team"
    }
}