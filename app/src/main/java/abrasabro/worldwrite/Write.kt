package abrasabro.worldwrite


data class Write(
        var message: String = "",
        var lat: Double = 0.0,
        var lon: Double = 0.0,
        var address: String = "",
        var ratingGood: Int = 0,
        var ratingPoor: Int = 0,
        var messageUID: String = "")