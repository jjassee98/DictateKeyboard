/*
 * Copyright (C) 2026 Jasse Keyboard contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package dev.patrickgold.florisboard.ime.nlp.latin

import java.util.Locale

/** Local Swedish spelling and spacing evidence, shared by the IME and its regression tests. */
internal object SwedishCorrections {
    data class Proposal(val text: String, val mayAutoCommit: Boolean)

    // Unambiguous spelling errors, not grammar/style substitutions (de/dem, dom, compounds, etc.).
    // Targets must also exist in the installed dictionary. Ordinary key slips use CorrectionReaders.
    private val spellings = mapOf(
        "igentligen" to "egentligen", "egentilgen" to "egentligen",
        "värkligen" to "verkligen", "faktist" to "faktiskt",
        "desutom" to "dessutom", "dessumtom" to "dessutom",
        "tilsammans" to "tillsammans", "tillsamans" to "tillsammans",
        "intresant" to "intressant", "interesant" to "intressant",
        "intreserad" to "intresserad",
        "själklart" to "självklart", "självkart" to "självklart",
        "mäniskor" to "människor", "människer" to "människor",
        "tyverr" to "tyvärr", "tyvär" to "tyvärr",
        "åtminstonde" to "åtminstone", "åtminstonne" to "åtminstone",
        "noggran" to "noggrann", "annolunda" to "annorlunda",
        "förän" to "förrän", "åtminståne" to "åtminstone",
        "förhopningsvis" to "förhoppningsvis",
        "rekomendera" to "rekommendera", "rekomenderar" to "rekommenderar",
    )
    private val pronouns = setOf("jag", "du", "vi", "ni", "han", "hon", "de", "det", "den")
    private val starters = pronouns + setOf(
        "kan", "ska", "vill", "har", "är", "inte", "att", "och", "men", "som", "för", "med",
        "till", "om", "när", "var", "hur", "vad",
    )

    fun propose(
        word: String,
        frequency: (String) -> Int,
        pairCount: (String) -> Long,
        protectedWord: Boolean,
        strength: AutoCorrectStrength,
        spellingEnabled: Boolean = true,
        spacingEnabled: Boolean = true,
    ): List<Proposal> {
        if (protectedWord || word.length !in 3..32 || !word.all { it.isLetter() }) return emptyList()
        // Preserve acronyms, mixed-case names and identifiers. Sentence-initial capitals are retained.
        if (word.drop(1).any { it.isUpperCase() }) return emptyList()
        val lower = word.lowercase(Locale.ROOT)
        if (frequency(lower) > 0) return emptyList()
        fun cased(text: String) = if (word.first().isUpperCase()) {
            text.replaceFirstChar { it.uppercaseChar() }
        } else text

        if (spellingEnabled) {
            val target = spellings[lower]
            if (target != null && frequency(target) > 0) {
                return listOf(Proposal(cased(target), mayAutoCommit = frequency(target) >= AutoCommitGate.MIN_FREQ))
            }
        }
        if (!spacingEnabled || word.length < 5) return emptyList()
        data class Split(val text: String, val count: Long, val starter: String)
        val splits = starters.mapNotNull { left ->
            if (!lower.startsWith(left) || lower.length - left.length < 2) return@mapNotNull null
            val right = lower.substring(left.length)
            if (frequency(left) < 170 || frequency(right) < 170) return@mapNotNull null
            val phrase = "$left $right"
            val count = pairCount(phrase)
            // Without corpus evidence, only offer pronoun + a frequent word for an explicit tap.
            if (count < 3 && !(left in pronouns && frequency(right) >= 180)) return@mapNotNull null
            Split(phrase, count, left)
        }.sortedWith(compareByDescending<Split> { it.count }.thenBy { it.text })
        val threshold = when (strength) {
            AutoCorrectStrength.CAUTIOUS -> 100L
            AutoCorrectStrength.BALANCED -> 20L
            AutoCorrectStrength.AGGRESSIVE -> 5L
        }
        return splits.take(2).mapIndexed { i, split ->
            val runnerUp = splits.getOrNull(1)?.count ?: 0L
            Proposal(
                text = cased(split.text),
                // Productive Swedish noun compounds must not be split merely because two parts exist.
                mayAutoCommit = i == 0 && split.starter in pronouns && split.count >= threshold &&
                    (runnerUp == 0L || split.count / 4 >= runnerUp),
            )
        }
    }
}
