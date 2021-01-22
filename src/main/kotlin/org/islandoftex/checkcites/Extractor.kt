package org.islandoftex.checkcites

import java.io.File

sealed class Mode(val keyRegex: Regex, val bibRegex: Regex) {
    class Biber : Mode("<bcf:citekey[^>]*>(.+?)</bcf:citekey>".toRegex(),
        "<bcf:datasource[^>]*>(.+?)</bcf:datasource>".toRegex())
    class BibTeX : Mode("\\\\citation\\{(.*?)\\}".toRegex(),
        "\\\\bibdata\\{(.*?)\\}".toRegex())
}

data class CitationData(val citations: Set<String>, val bibliographies: Set<String>)

class Extractor(private val mode: Mode) {

    fun extract(files: List<File>) {

        val dataMap = files.map {
            val text = it.readLines().joinToString(" ")

            it to CitationData(
                mode.keyRegex.findAll(text).map { result ->
                    result.groupValues[1].split(",").map { s -> s.trim() }
                }.flatten().toSet(),
                mode.bibRegex.findAll(text).map { result ->
                    result.groupValues[1].split(",").map { s -> s.trim() }
                }.flatten().toSet()
            )
        }.toMap()

        println(dataMap)
    }

}