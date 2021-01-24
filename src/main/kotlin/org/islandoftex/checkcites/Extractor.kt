// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.checkcites

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.readLines

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

data class CitationData(val citations: Set<String>, val bibliographies: Set<Path>)
data class LookupResolution(val searchPaths: List<Path>, val searchTree: Boolean)

class Extractor(private val mode: Mode, private val resolution: LookupResolution) {

    @ExperimentalPathApi
    fun extract(files: List<Path>) {

        val dataMap = files.map {
            val text = it.readLines().joinToString()

            it to CitationData(
                mode.keyRegex.findAll(text).map { result ->
                    result.groupValues[1].split(",").map { s -> s.trim() }
                }.flatten().toSet(),
                mode.bibRegex.findAll(text).map { result ->
                    result.groupValues[1].split(",")
                        .map { s -> s.trim().takeIf { t -> t.endsWith(".bib") } ?: "${s.trim()}.bib" }.map { s ->
                            listOf(Path(".")).plus(resolution.searchPaths).map { r ->
                                r / s
                            }
                                .firstOrNull { r -> r.exists() }
                                ?: Kpsewhich.takeIf { resolution.searchTree }
                                    ?.getHitFor(s)
                                    ?.takeIf { r -> r.isNotBlank() }
                                    ?.let { r -> Path(r) }
                                ?: throw Exception(
                                    "Bibliography not found: $s"
                                )
                        }
                }.flatten().toSet()
            )
        }.toMap()

        println(dataMap)
    }
}
