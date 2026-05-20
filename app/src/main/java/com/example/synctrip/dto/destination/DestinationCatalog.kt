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

    val ALL: List<DestinationResponse> = listOf(
        // 일본
        DestinationResponse("도쿄", "일본", "JP", 35.6762, 139.6503, true, "일본", "신주쿠, 시부야, 아키하바라", "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=400&q=80"),
        DestinationResponse("오사카", "일본", "JP", 34.6937, 135.5023, true, "일본", "오사카, 교토, 고베, 나라", "https://images.unsplash.com/photo-1528360983277-13d401cdc186?w=400&q=80"),
        DestinationResponse("후쿠오카", "일본", "JP", 33.5904, 130.4017, true, "일본", "후쿠오카, 유후인, 벳부", "https://images.unsplash.com/photo-1590559899731-a382839e5549?w=400&q=80"),
        DestinationResponse("삿포로", "일본", "JP", 43.0618, 141.3545, true, "일본", "삿포로, 하코다테, 오타루", "https://images.unsplash.com/photo-1536098561742-ca998e48cbcc?w=400&q=80"),
        DestinationResponse("나고야", "일본", "JP", 35.1815, 136.9066, true, "일본", "나고야, 다카야마, 시라카와고", "https://images.unsplash.com/photo-1524413840807-0c3cb6fa808d?w=400&q=80"),

        // 동남아시아
        DestinationResponse("방콕", "태국", "TH", 13.7563, 100.5018, true, "동남아시아", "방콕, 파타야, 아유타야", "https://images.unsplash.com/photo-1563492065599-3520f775eeed?w=400&q=80"),
        DestinationResponse("싱가포르", "싱가포르", "SG", 1.3521, 103.8198, true, "동남아시아", "마리나베이, 센토사, 가든스바이더베이", "https://images.unsplash.com/photo-1525625293386-3f8f99389edd?w=400&q=80"),
        DestinationResponse("발리", "인도네시아", "ID", -8.3405, 115.0920, true, "동남아시아", "꾸따, 우붓, 스미냑", "https://images.unsplash.com/photo-1537996194471-e657df975ab4?w=400&q=80"),
        DestinationResponse("다낭", "베트남", "VN", 16.0544, 108.2022, true, "동남아시아", "다낭, 호이안, 후에", "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=400&q=80"),
        DestinationResponse("세부", "필리핀", "PH", 10.3157, 123.8854, true, "동남아시아", "세부, 보홀, 막탄", "https://images.unsplash.com/photo-1573551089778-46a7abc39d9f?w=400&q=80"),

        // 유럽
        DestinationResponse("파리", "프랑스", "FR", 48.8566, 2.3522, true, "유럽", "에펠탑, 루브르, 샹젤리제", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=400&q=80"),
        DestinationResponse("런던", "영국", "GB", 51.5074, -0.1278, true, "유럽", "빅벤, 타워브리지, 버킹엄궁전", "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=400&q=80"),
        DestinationResponse("바르셀로나", "스페인", "ES", 41.3851, 2.1734, true, "유럽", "사그라다파밀리아, 람블라스, 가우디", "https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=400&q=80"),
        DestinationResponse("로마", "이탈리아", "IT", 41.9028, 12.4964, true, "유럽", "콜로세움, 트레비분수, 바티칸", "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=400&q=80"),
        DestinationResponse("암스테르담", "네덜란드", "NL", 52.3676, 4.9041, true, "유럽", "운하, 반고흐미술관, 안네의집", "https://images.unsplash.com/photo-1534351590666-13e3e96b5017?w=400&q=80"),

        // 미주/오세아니아
        DestinationResponse("뉴욕", "미국", "US", 40.7128, -74.0060, true, "미주/오세아니아", "맨해튼, 센트럴파크, 자유의여신상", "https://images.unsplash.com/photo-1534430480872-3498386e7856?w=400&q=80"),
        DestinationResponse("하와이", "미국", "US", 21.3069, -157.8583, true, "미주/오세아니아", "와이키키, 마우이, 빅아일랜드", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400&q=80"),
        DestinationResponse("시드니", "호주", "AU", -33.8688, 151.2093, true, "미주/오세아니아", "오페라하우스, 본다이비치, 블루마운틴", "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?w=400&q=80"),

        // 중화권
        DestinationResponse("홍콩", "홍콩", "HK", 22.3193, 114.1694, true, "중화권", "빅토리아피크, 침사추이, 란콰이펑", "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?w=400&q=80"),
        DestinationResponse("타이베이", "대만", "TW", 25.0330, 121.5654, true, "중화권", "지우펀, 101빌딩, 예류", "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a?w=400&q=80"),

        // 국내
        DestinationResponse("서울", "대한민국", "KR", 37.5665, 126.9780, false, "국내", "경복궁, 홍대, 명동, 한강", "https://images.unsplash.com/photo-1546874177-9e664107314e?w=400&q=80"),
        DestinationResponse("제주", "대한민국", "KR", 33.4996, 126.5312, false, "국내", "성산일출봉, 한라산, 협재해수욕장", "https://images.unsplash.com/photo-1598935898639-81586f7d2129?w=400&q=80"),
        DestinationResponse("부산", "대한민국", "KR", 35.1796, 129.0756, false, "국내", "해운대, 광안리, 감천문화마을", "https://plus.unsplash.com/premium_photo-1661963645994-e1303df0d8c8?w=400&q=80"),
        DestinationResponse("경주", "대한민국", "KR", 35.8562, 129.2247, false, "국내", "불국사, 첨성대, 동궁과월지", "https://images.unsplash.com/photo-1589394815804-964ed0be2eb5?w=400&q=80"),
        DestinationResponse("속초", "대한민국", "KR", 38.2070, 128.5919, false, "국내", "설악산, 청초호, 속초해수욕장", "https://images.unsplash.com/photo-1601128533718-374ffcca299b?w=400&q=80"),
        DestinationResponse("강릉", "대한민국", "KR", 37.7519, 128.8760, false, "국내", "경포대, 정동진, 안목커피거리", "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&q=80"),
        DestinationResponse("전주", "대한민국", "KR", 35.8242, 127.1480, false, "국내", "전주한옥마을, 비빔밥, 막걸리골목", "https://images.unsplash.com/photo-1582721478779-0ae163c05a60?w=400&q=80"),
        DestinationResponse("여수", "대한민국", "KR", 34.7604, 127.6622, false, "국내", "돌산도, 여수밤바다, 오동도", "https://images.unsplash.com/photo-1578469645742-46cae010e5d4?w=400&q=80")
    )

    val TOP_PICKS: List<DestinationResponse> = listOf(
        ALL.first { it.name == "도쿄" },
        ALL.first { it.name == "오사카" },
        ALL.first { it.name == "파리" },
        ALL.first { it.name == "뉴욕" },
        ALL.first { it.name == "방콕" },
        ALL.first { it.name == "발리" },
        ALL.first { it.name == "런던" },
        ALL.first { it.name == "하와이" },
        ALL.first { it.name == "제주" },
        ALL.first { it.name == "서울" }
    )

    fun byOverseas(overseas: Boolean): List<DestinationResponse> = ALL.filter { it.overseas == overseas }
    fun byRegion(region: String): List<DestinationResponse> = ALL.filter { it.region == region }
    fun search(query: String): List<DestinationResponse> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return ALL.filter {
            it.name.lowercase().contains(q) ||
            it.country.lowercase().contains(q) ||
            (it.description?.lowercase()?.contains(q) == true)
        }
    }
}
