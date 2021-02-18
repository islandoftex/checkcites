// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.checkcites

import java.nio.file.Paths

fun main(args: Array<String>) {
    println("Hello checkcites")
    val x = Bibliography().extract(listOf(Paths.get("tugboat.bib")), setOf(), setOf())
    println(x)
}
