package com.example.synctrip.dto.destination

object DestinationCatalog {

    fun flagOf(countryCode: String?): String = when (countryCode) {
        "JP" -> "🇯🇵"
        "TH" -> "🇹🇭"
        "SG" -> "🇸🇬"
        "ID" -> "🇮🇩"
        "VN" -> "🇻🇳"
        "PH" -> "🇵🇭"
        "FR" -> "🇫🇷"
        "GB" -> "🇬🇧"
        "ES" -> "🇪🇸"
        "IT" -> "🇮🇹"
        "NL" -> "🇳🇱"
        "US" -> "🇺🇸"
        "AU" -> "🇦🇺"
        "HK" -> "🇭🇰"
        "TW" -> "🇹🇼"
        "KR" -> "🇰🇷"
        else -> "🌍"
    }

    val OVERSEAS_REGIONS = listOf("일본", "동남아시아", "유럽", "미주/오세아니아", "중화권")
    val DOMESTIC_REGIONS = listOf("국내")

    val TOP_PICK_NAMES = listOf("도쿄", "오사카", "파리", "뉴욕", "방콕", "발리", "런던", "하와이", "제주", "서울")
}
