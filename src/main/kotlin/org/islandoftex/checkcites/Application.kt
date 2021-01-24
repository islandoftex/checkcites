// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.checkcites

import java.nio.file.Path

fun main(args: Array<String>) {
    println("Hello checkcites")
    val extractor = Extractor(Mode.Biber(), LookupResolution(emptyList(), true))
    extractor.extract(listOf(Path.of("test.bcf")))
}
