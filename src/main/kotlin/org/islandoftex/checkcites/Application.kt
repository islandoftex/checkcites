// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.checkcites

import java.io.File

fun main(args: Array<String>) {
    println("Hello checkcites")

    val extractor = Extractor(Mode.Biber(), LookupResolution(emptyList(), true))
    extractor.extract(listOf(File("test.bcf")))
}
