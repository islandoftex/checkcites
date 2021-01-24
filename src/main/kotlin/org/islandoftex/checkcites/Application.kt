// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.checkcites

fun main(args: Array<String>) {
    println("Hello checkcites")
    val extractor = Extractor(Mode.Biber(), LookupResolution(emptyList(), true))
}
