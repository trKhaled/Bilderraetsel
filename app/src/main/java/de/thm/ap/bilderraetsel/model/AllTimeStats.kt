package de.thm.ap.bilderraetsel.model



/**
 * Statistic model of all the results combined
 */
data class AllTimeStats (

    var correct: Int = 0,
    var notAnswered: Int = 0,
    var score: Int = 0,
    var wrong: Int = 0,
    var gamesNum: Int = 0

)