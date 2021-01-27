package org.islandoftex.checkcites

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readLines

data class BibliographyData(val type: String, val key: String, val crossrefs: Set<String>)

class Bibliography {

    @ExperimentalPathApi
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
                    p.value.first.toLowerCase(),
                    p.value.second,
                    crossrefs[mapping[p.key]] ?: emptySet()
                )
            }.distinctBy { p -> p.key }.toSet()
        }.toMap()
    }

}