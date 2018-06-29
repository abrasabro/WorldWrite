package abrasabro.worldwrite


data class User(
        var userUID: String = "",
        var goodRatings: MutableList<String> = mutableListOf(),
        var poorRatings: MutableList<String> = mutableListOf())