package com.valladares.iptvplayer.data.xtream

/**
 * Heuristic detector for ISO country codes from channel/category names.
 */
object CountryCodeDetector {
    private val countryPatterns: Map<String, List<String>> = mapOf(
        "SE" to listOf("SE", "SV", "SWEDEN", "SWEDISH", "SVERIGE", "🇸🇪"),
        "US" to listOf("US", "USA", "UNITED STATES", "🇺🇸"),
        "GB" to listOf("GB", "UK", "BRITAIN", "BRITISH", "ENGLAND", "🇬🇧"),
        "DE" to listOf("DE", "GERMANY", "GERMAN", "DEUTSCH", "🇩🇪"),
        "FI" to listOf("FI", "FINLAND", "FINNISH", "SUOMI", "🇫🇮"),
        "NO" to listOf("NO", "NOR", "NORWAY", "NORWEGIAN", "NORGE", "🇳🇴"),
        "DK" to listOf("DK", "DEN", "DENMARK", "DANISH", "DANMARK", "🇩🇰"),
        "NL" to listOf("NL", "NETHERLANDS", "DUTCH", "HOLLAND", "🇳🇱"),
        "ES" to listOf("ES", "SPAIN", "SPANISH", "ESPANA", "🇪🇸"),
        "IT" to listOf("IT", "ITALY", "ITALIAN", "🇮🇹"),
        "FR" to listOf("FR", "FRANCE", "FRENCH", "🇫🇷"),
        "PT" to listOf("PT", "PORTUGAL", "PORTUGUESE", "🇵🇹"),
        "BR" to listOf("BR", "BRAZIL", "BRASIL", "BRAZILIAN", "🇧🇷"),
        "MX" to listOf("MX", "MEXICO", "MEXICAN", "🇲🇽"),
        "AR" to listOf("AR", "ARGENTINA", "🇦🇷"),
        "TR" to listOf("TR", "TURKEY", "TURKISH", "TURKIYE", "🇹🇷"),
        "PL" to listOf("PL", "POLAND", "POLISH", "POLSKA", "🇵🇱"),
        "RU" to listOf("RU", "RUSSIA", "RUSSIAN", "🇷🇺"),
        "IN" to listOf("IN", "INDIA", "HINDI", "🇮🇳"),
        "CN" to listOf("CN", "CHINA", "CHINESE", "🇨🇳")
    )

    /**
     * Försöker detektera landskod från kanalnamn och kategorinamn.
     *
     * Regler (i ordning):
     * 1. Prefix i hakparentes: "[SE]", "[US]", "(SE)".
     * 2. Prefix med kolon: "SE:", "SE |", "SE -".
     * 3. Emoji-flagga någonstans i strängen.
     * 4. Ord på egen position (omgivet av whitespace eller gränsmarkörer).
     * 5. Kategorinamn fallback.
     */
    fun detect(channelName: String, categoryName: String?): String? {
        val channelUpper = channelName.uppercase()
        val categoryUpper = categoryName?.uppercase().orEmpty()

        val bracketMatch = Regex("^[\\[\\(]([A-Z]{2})[\\]\\)]").find(channelUpper)
        if (bracketMatch != null) {
            val code = bracketMatch.groupValues[1]
            if (countryPatterns.containsKey(code)) {
                return code
            }
        }

        val prefixMatch = Regex("^([A-Z]{2,3})\\s*[:|\\-]").find(channelUpper)
        if (prefixMatch != null) {
            val code = prefixMatch.groupValues[1]
            if (countryPatterns.containsKey(code)) {
                return code
            }
            countryPatterns.entries.firstOrNull { (_, aliases) ->
                code in aliases
            }?.let { return it.key }
        }

        countryPatterns.entries.firstOrNull { (_, aliases) ->
            aliases.any { alias ->
                alias.length > 2 && channelName.contains(alias)
            }
        }?.let { return it.key }

        countryPatterns.entries.firstOrNull { (_, aliases) ->
            aliases.any { alias ->
                Regex("\\b${Regex.escape(alias)}\\b").containsMatchIn(channelUpper)
            }
        }?.let { return it.key }

        if (categoryUpper.isNotBlank()) {
            countryPatterns.entries.firstOrNull { (_, aliases) ->
                aliases.any { alias -> categoryUpper.contains(alias) }
            }?.let { return it.key }
        }

        return null
    }
}
