// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.checkcites

import java.io.File

fun main(args: Array<String>) {
    println("Hello checkcites")
    val extractor = Extractor(Mode.BibTeX())
    extractor.extract(listOf(File("test.aux")))
}
