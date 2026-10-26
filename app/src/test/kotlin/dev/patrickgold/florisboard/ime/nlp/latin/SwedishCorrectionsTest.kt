/*
 * Copyright (C) 2026 Jasse Keyboard contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package dev.patrickgold.florisboard.ime.nlp.latin

import java.security.MessageDigest
import dev.patrickgold.florisboard.ime.nlp.WordOrigin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SwedishCorrectionsTest {
    private val words by lazy { EvalKeyboard.readDict("sv.json").mapKeys { it.key.lowercase() } }
    private val pairs by lazy { NgramIndex.parse(EvalKeyboard.dictFile("sv_bigrams.txt").readText()) }
    private fun propose(
        word: String,
        protected: Boolean = false,
        strength: AutoCorrectStrength = AutoCorrectStrength.BALANCED,
        spelling: Boolean = true,
        spacing: Boolean = true,
    ) = SwedishCorrections.propose(
        word, { words[it] ?: 0 }, pairs::countOf, protected, strength, spelling, spacing,
    )

    @Test
    fun commonMisspellingsReachTheRealSwedishDictionary() {
        val cases = mapOf(
            "igentligen" to "egentligen", "värkligen" to "verkligen", "faktist" to "faktiskt",
            "desutom" to "dessutom", "tillsamans" to "tillsammans", "intresant" to "intressant",
            "interesant" to "intressant", "själklart" to "självklart", "mäniskor" to "människor",
            "tyverr" to "tyvärr", "tyvär" to "tyvärr", "åtminstonde" to "åtminstone",
            "noggran" to "noggrann", "annolunda" to "annorlunda", "förän" to "förrän",
            "rekomendera" to "rekommendera", "rekomenderar" to "rekommenderar",
        )
        for ((typo, expected) in cases) {
            assertEquals(expected, propose(typo).firstOrNull()?.text, typo)
            assertEquals((words[expected] ?: 0) >= AutoCommitGate.MIN_FREQ, propose(typo).first().mayAutoCommit, typo)
        }
        assertEquals("Egentligen", propose("Igentligen").first().text)
    }

    @Test
    fun missingSpacesUseRealPhraseEvidence() {
        for ((joined, expected) in mapOf(
            "jagvet" to "jag vet", "detär" to "det är", "jagkommer" to "jag kommer",
            "dukommer" to "du kommer", "jaghar" to "jag har", "jagvill" to "jag vill",
            "duvet" to "du vet",
        )) {
            assertEquals(expected, propose(joined).first().text, joined)
            assertTrue(propose(joined).first().mayAutoCommit, joined)
        }
        // The requested case is absent from the corpus. Recognise it, but require an explicit tap.
        assertEquals("du märker", propose("dumärker").first().text)
        assertFalse(propose("dumärker").first().mayAutoCommit)
        assertEquals("Jag vet", propose("Jagvet").first().text)
        assertEquals("kan du", propose("kandu").first().text)
        assertFalse(propose("kandu").first().mayAutoCommit)
    }

    @Test
    fun correctWordsCompoundsNamesAndIdentifiersStayAsWritten() {
        for (word in listOf(
            "tangentbord", "sjuksköterska", "mellanslag", "sommarstuga", "Stockholm",
            "imorgon", "medans", "dom", "absolut", "vises", "förskola", "jaguar",
            "JasseKeyboard", "JAGVET", "S23Ultra", "jag@example.se", "https://jagvet.se",
        )) assertTrue(propose(word).isEmpty(), word)
        assertTrue(propose("dumärker", protected = true).isEmpty())
        assertTrue(propose("igentligen", protected = true).isEmpty())
        // The IME passes protected=true for personal words and words accepted in English.
        assertTrue(propose("whatever", protected = true).isEmpty())
    }

    @Test
    fun strengthAndFeatureSwitchesHaveAnEffect() {
        assertFalse(propose("duvet", strength = AutoCorrectStrength.CAUTIOUS).first().mayAutoCommit)
        assertTrue(propose("duvet", strength = AutoCorrectStrength.BALANCED).first().mayAutoCommit)
        assertTrue(propose("dumärker", spacing = false).isEmpty())
        assertTrue(propose("igentligen", spelling = false).isEmpty())
        assertTrue(propose("dumärker", spelling = false).isNotEmpty())
    }

    @Test
    fun ordinaryKeySlipsStillReachTheExistingStringReader() {
        val alphabet = words.keys.flatMap { it.toList() }.toSet() + '\''
        for ((typo, expected) in mapOf(
            "tangetnbord" to "tangentbord", "svergie" to "sverige", "mellansalg" to "mellanslag",
            "verklgien" to "verkligen", "tillsammnas" to "tillsammans", "faktikst" to "faktiskt",
        )) {
            val fixes = CorrectionReaders.byEditDistance(
                folded = typo, freq = words, alphabet = alphabet, prefixIndex = null,
                maxCount = 3, allowDistance2 = false, sqDistance = { _, _ -> null },
            )
            assertTrue(expected in fixes, "$typo -> $fixes; expected $expected")
        }
    }

    @Test
    fun bundledAssetsMatchTheAttributedPinnedUpstreamFiles() {
        val hashes = mapOf(
            "sv.json" to "95dd05108d3035cd7dd501accd4dfa60e20da4ae11adf8f082941fab277f7cee",
            "sv_bigrams.txt" to "f413d0743258d3685d518a8b997e7388c1f1c68869ba7c99d41ad22011dc7d1b",
            "sv_trigrams.txt" to "dbaeadc182716d9c6650263f9dcc14f541294717145cfb5ce104ba0e284a0dda",
        )
        for ((file, expected) in hashes) {
            val digest = MessageDigest.getInstance("SHA-256").digest(EvalKeyboard.dictFile(file).readBytes())
                .joinToString("") { "%02x".format(it.toInt() and 0xff) }
            assertEquals(expected, digest, file)
        }
        val trigrams = NgramIndex.parse(EvalKeyboard.dictFile("sv_trigrams.txt").readText())
        assertTrue(trigrams.topContinuations("det är", 3).isNotEmpty())
        assertTrue(pairs.topContinuations("jag", 3).isNotEmpty())
    }

    @Test
    fun ignoredTypoSuggestionsMustNotBeLearnedAsIntentionalWords() {
        for (typo in listOf("dumärker", "jagvet", "igentligen")) {
            assertFalse(WordLearningGate.shouldLearn(
                enabled = true, isPrivateField = false, origin = WordOrigin.TYPED,
                word = typo, isKnownWord = false, cheapCorrectionExists = propose(typo).isNotEmpty(),
            ), typo)
        }
    }

    @Test
    fun typedEvidenceDoesNotCarryAcrossDictationPasteOrCursorMoves() {
        TouchTrace.reset()
        "jagvet".forEach { TouchTrace.commit(it.toString()) }
        assertTrue(TouchTrace.wasFullyTyped("jagvet"))
        assertFalse(TouchTrace.wasFullyTyped("dumärker"))
        TouchTrace.reset()
        TouchTrace.commit("dumärker") // Whole-string input is a paste/dictation, not individual key events.
        assertFalse(TouchTrace.wasFullyTyped("dumärker"))
    }
}
