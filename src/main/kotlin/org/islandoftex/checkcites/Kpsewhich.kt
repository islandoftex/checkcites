package org.islandoftex.checkcites

import java.io.IOException

object Kpsewhich {

    @Throws(IOException::class)
    private fun getCommandOutput(command: List<String>): List<String> {
        return ProcessBuilder(command).start().inputStream
            .bufferedReader().useLines { it.toList() }
    }

    @JvmStatic
    val version: String
        @Throws(IOException::class)
        get() = getCommandOutput(listOf("kpsewhich", "--version"))[0]

    @JvmStatic
    val isAvailable: Boolean by lazy {
        runCatching { version.isNotBlank() }.getOrElse { false }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun getHitFor(query: String): String =
        runCatching { getCommandOutput(listOf("kpsewhich", query))[0] }.getOrElse { "" }
}
