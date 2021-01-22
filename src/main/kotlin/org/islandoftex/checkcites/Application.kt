// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.checkcites

import java.io.File

fun main(args: Array<String>) {
    println("Hello checkcites")
    val extractor = Extractor(Mode.BIBTEX)
    extractor.extract(listOf(File("/home/paulo/Testes/test.aux")))
}
