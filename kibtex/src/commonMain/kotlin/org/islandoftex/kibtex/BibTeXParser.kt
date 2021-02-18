// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.kibtex

public data class BibliographyEntry(val type: String, val key: String? = null, val fields: Map<String, String>)

public enum class TextElement {
    BRACKET,
    QUOTE,
    VARIABLE,
    DIGIT,
    CONCAT,
    EMPTY
}

public class BibTeXParser {
    public fun parseText(text: String): List<BibliographyEntry> {
        return extractBlocks(text)
            .map { it.first.trim() to it.second.trim() }
            .map {
                when (it.first) {
                    "comment" -> parseLiteral(it.first, it.second)
                    "preamble" -> parseString(it.first, it.second)
                    "string" -> parseString(it.first, it.second)
                    else -> parseEntry(it.first, it.second)
                }
            }
    }

    private fun extractBlocks(text: String): List<Pair<String, String>> {
        val entries = "@([a-zA-Z]+)\\s*([\\{\\(])".toRegex().findAll(text)

        val blocks = entries.mapNotNull {
            var (bracketCounter, parenthesisCounter) = when (it.groupValues[2]) {
                "{" -> 1 to 0
                else -> 0 to 1
            }

            var limit = -1
            for (i in (it.range.last + 1) until text.length) {
                when (text[i]) {
                    '{' -> bracketCounter++
                    '}' -> bracketCounter--
                    '(' -> parenthesisCounter++
                    ')' -> parenthesisCounter--
                }
                if (bracketCounter < 0 || parenthesisCounter < 0) {
                    // TODO throw error/warning, unbalanced nesting at position $i
                    break
                } else if (bracketCounter == 0 && parenthesisCounter == 0) {
                    limit = i
                    break
                }
            }

            limit.takeUnless { _ ->
                !setOf("string", "preamble").contains(it.groupValues[1].toLowerCase()) &&
                        it.groupValues[2] == "("
            }?.let { _ ->
                Triple(it.groupValues[1].toLowerCase(), it.range.last + 1, limit)
            }
        }.toList()

        return blocks.filterIndexed { index, triple ->
            index == 0 || blocks[index - 1].third < triple.third
        }.map {
            it.first to text.substring(it.second, it.third)
        }
    }

    private fun parseLiteral(type: String, text: String): BibliographyEntry =
        BibliographyEntry(type, null, mapOf("content" to text))

    private fun parseString(type: String, text: String): BibliographyEntry {
        val map = "^\\s*([a-zA-Z][a-zA-Z0-9_-]+)\\s*=\\s*"
            .toRegex(RegexOption.MULTILINE)
            .find(text)
            ?.let {
                val (value, _) = extractLine(text.substring(it.range.last).trim())
                mapOf(it.groupValues[1] to value.joinToString(" "))
            }
            ?: mapOf()

        return BibliographyEntry(type, null, map)
    }

    private fun parseEntry(type: String, text: String): BibliographyEntry {
        var key: String? = null
        var range = IntRange.EMPTY
        val fields = mutableMapOf<String, String>()

        "^\\s*([^ ,{}()\\[\\]]*)\\s*,".toRegex().find(text)?.let {
            range = IntRange(it.range.last, text.length - 1)
            key = it.groupValues[1]
        }

        while (!range.isEmpty()) {
            val pair = findKey(text, range.first)

            if (!pair.second.isEmpty()) {
                range = IntRange(pair.second.last, text.length - 1)
                val value = extractLine(text.substring(range).trim())

                if (value.first.isNotEmpty() && pair.first !in fields) {
                    fields[pair.first] = value.first.joinToString(" ")
                }

                range = IntRange(value.second + pair.second.last, text.length - 1)
            } else {
                range = IntRange.EMPTY
            }
        }

        return BibliographyEntry(type, key, fields)
    }

    private fun findKey(text: String, start: Int): Pair<String, IntRange> {
        return ",.*?\\s*([a-zA-Z]\\w+)\\s*=\\s*"
            .toRegex()
            .find(text, start)
            ?.let {
                it.groupValues[1] to it.range
            }
            ?: ("" to IntRange.EMPTY)
    }

    private fun extractLine(text: String): Pair<List<String>, Int> {
        var range = IntRange(0, text.length - 1)
        var element = TextElement.EMPTY
        val result = mutableListOf<String>()
        var limit = 0

        while (!range.isEmpty()) {
            val pair = when (text[range.first]) {
                '{' -> Pair(getBracketText(text, range.first, element), TextElement.BRACKET)
                '"' -> Pair(getQuoteText(text, range.first, element), TextElement.QUOTE)
                else -> getComplement(text, range.first, element)
            }

            range = pair.first
            element = pair.second

            result.takeIf { !range.isEmpty() }?.let {
                result.add(text.substring(range).trim())
                range = IntRange(range.last + 1, text.length - 1)
                limit = range.first
            }
        }

        return Pair(result, limit)
    }

    private fun getBracketText(text: String, start: Int, element: TextElement): IntRange {
        if (!setOf(TextElement.EMPTY, TextElement.CONCAT).contains(element)) return IntRange.EMPTY

        var bracketCounter = 0
        var parenthesisCounter = 0
        var limit = -1
        val interval = IntRange(start, text.length - 1)
        for (i in interval) {
            when (text[i]) {
                '{' -> bracketCounter++
                '}' -> bracketCounter--
                '(' -> parenthesisCounter++
                ')' -> parenthesisCounter--
                '@' -> return IntRange.EMPTY
            }

            if (bracketCounter == 0 && parenthesisCounter == 0) {
                limit = i
                break
            }
        }

        return if (limit != -1) IntRange(start, limit) else IntRange.EMPTY
    }

    private fun getQuoteText(text: String, start: Int, element: TextElement): IntRange {
        if (!setOf(TextElement.EMPTY, TextElement.CONCAT).contains(element))
            return IntRange.EMPTY

        var bracketCounter = 0
        var stop = false
        var limit = -1
        val interval = IntRange(start, text.length - 1)
        for (i in interval) {
            when (text[i]) {
                '{' -> bracketCounter++
                '}' -> bracketCounter--
                '"' -> stop = (i != start && bracketCounter == 0)
            }

            if (stop) {
                limit = i
                break
            }
        }

        return if (limit != -1) IntRange(start, limit) else IntRange.EMPTY
    }

    private fun getComplement(text: String, start: Int, element: TextElement): Pair<IntRange, TextElement> {
        val slice = text.substring(start)

        val pair = "^(\\s+#\\s+[\\{\"\\da-zA-Z])".toRegex().find(slice)?.range?.let {
            Pair(IntRange(it.first + start, it.last + start - 1), TextElement.CONCAT)
        } ?: "^(\\s*[a-zA-Z][a-zA-Z0-9_-]*)".toRegex().find(slice)?.range?.let {
            Pair(IntRange(it.first + start, it.last + start), TextElement.VARIABLE)
        }
        ?: "^(\\s*\\d+)".toRegex().find(slice)?.range?.let {
            Pair(IntRange(it.first + start, it.last + start), TextElement.DIGIT)
        } ?: "^(\\s*,)".toRegex().find(slice)?.let {
            Pair(IntRange.EMPTY, TextElement.EMPTY)
        }
        ?: Pair(IntRange.EMPTY, TextElement.EMPTY)

        val valid = when (element) {
            TextElement.EMPTY -> setOf(TextElement.DIGIT, TextElement.VARIABLE).contains(pair.second)
            TextElement.CONCAT -> setOf(TextElement.DIGIT, TextElement.VARIABLE).contains(pair.second)
            else -> pair.second == TextElement.CONCAT
        }

        return if (valid) pair else Pair(IntRange.EMPTY, TextElement.EMPTY)
    }
}
