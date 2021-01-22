package org.islandoftex.checkcites

import java.io.File

enum class Mode {
    BIBTEX,
    BIBER
}

class Extractor(private val mode: Mode) {

    fun extract(files: List<File>) {

        val (r1, r2) = when (mode) {
            Mode.BIBER -> Pair(
                "<bcf:citekey[^>]*>(.+?)</bcf:citekey>".toRegex(),
                "<bcf:datasource[^>]*>(.+?)</bcf:datasource>".toRegex()
            )
            Mode.BIBTEX -> Pair(
                "\\\\citation\\{(.*?)\\}".toRegex(),
                "\\\\bibdata\\{(.*?)\\}".toRegex()
            )
        }

        val citations = files.map {
            val text = it.readLines().joinToString(" ")

            Pair(
                r1.findAll(text).map { result ->
                    result.groupValues[1].split(",").map { s -> s.trim() }
                }.flatten().toSet(),
                r2.findAll(text).map { result ->
                    result.groupValues[1].split(",").map { s -> s.trim() }
                }.flatten().toSet()
            )
        }

        println(citations)
    }

}