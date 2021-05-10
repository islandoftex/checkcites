// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.checkcites

import java.nio.file.Path
import kotlin.io.path.readLines
import kotlin.io.path.readText
import org.islandoftex.kibtex.BibTeXParser

data class BibliographyData(val type: String, val key: String, val crossrefs: Set<String>)

class Bibliography {

    fun extract(
        files: List<Path>,
        ignoreTypes: Set<String>,
        ignoreKeys: Set<String>
    ): Map<Path, Set<BibliographyData>> {

        return files.map {
            val text = it.readText()

            it to BibTeXParser().parseText(text).filter { e ->
                if (e.key != null) !(ignoreKeys.contains(e.key) || ignoreTypes.contains(e.type)) else false
            }.map { e ->
                BibliographyData(e.type, e.key!!, e.fields["crossref"]?.removeSurrounding("\"")
                    ?.removeSurrounding("{", "}")?.split(",")?.map { s -> s.trim() }?.toSet()
                    ?: setOf())
            }.toSet()
        }.toMap()
    }

    fun extract(files: List<Path>): Map<Path, Set<BibliographyData>> {
        return files.map {
            val text = it.readLines().joinToString(" ")

            val keyRegex = "@(\\w+)\\s*\\{\\s*([^\\{\\s,\"]+)\\s*,".toRegex()
            val crossrefRegex = "\\bcrossref\\b\\s*=\\s*\\{(.+?)\\}".toRegex(RegexOption.IGNORE_CASE)

            val entries = keyRegex.findAll(text).map { match ->
                match.range.first to Pair(match.groupValues[1], match.groupValues[2])
            }.toMap()

            val crossrefs = crossrefRegex.findAll(text).map { match ->
                match.range.first to match.groupValues[1].split(",").map { s -> s.trim() }.toSet()
            }.toMap()

            val mapping = crossrefs.keys.map { i ->
                entries.keys.takeWhile { j -> j < i }.last()
            }.zip(crossrefs.keys).distinctBy { p -> p.first }.map { p -> p.first to p.second }.toMap()

            it to entries.map { p ->
                BibliographyData(
                    p.value.first.lowercase(),
                    p.value.second,
                    crossrefs[mapping[p.key]] ?: emptySet()
                )
            }.distinctBy { p -> p.key }.toSet()
        }.toMap()
    }
}
