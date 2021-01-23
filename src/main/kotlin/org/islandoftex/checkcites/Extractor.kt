package org.islandoftex.checkcites

import java.io.File
import java.nio.file.Paths

sealed class Mode(val keyRegex: Regex, val bibRegex: Regex) {
    class Biber : Mode(
        "<bcf:citekey[^>]*>(.+?)</bcf:citekey>".toRegex(),
        "<bcf:datasource[^>]*>(.+?)</bcf:datasource>".toRegex()
    )

    class BibTeX : Mode(
        "\\\\citation\\{(.*?)\\}".toRegex(),
        "\\\\bibdata\\{(.*?)\\}".toRegex()
    )
}

data class CitationData(val citations: Set<String>, val bibliographies: Set<File>)
data class LookupResolution(val searchPaths: List<File>, val searchTree: Boolean)

class Extractor(private val mode: Mode, private val resolution: LookupResolution) {

    fun extract(files: List<File>) {

        val dataMap = files.map {
            val text = it.readLines().joinToString()

            it to CitationData(
                mode.keyRegex.findAll(text).map { result ->
                    result.groupValues[1].split(",").map { s -> s.trim() }
                }.flatten().toSet(),
                mode.bibRegex.findAll(text).map { result ->
                    result.groupValues[1].split(",")
                        .map { s -> if (s.trim().endsWith(".bib")) s.trim() else "${s.trim()}.bib" }.map { s ->
                            listOf(File("."), *resolution.searchPaths.toTypedArray()).map { r ->
                                Paths.get(
                                    r.absolutePath,
                                    s
                                ).toFile()
                            }.firstOrNull { r -> r.exists() } ?: run {
                                if (resolution.searchTree) {
                                    val hit = Kpsewhich.getHitFor(s)
                                    if (hit.isNotBlank()) {
                                        File(hit)
                                    } else {
                                        // TODO throw exception
                                        throw Exception("bib not found: $s")
                                    }
                                } else {
                                    // TODO throw exception
                                    throw Exception("bib not found: $s")
                                }
                            }
                        }
                }.flatten().toSet()
            )
        }.toMap()

        println(dataMap)
    }

}