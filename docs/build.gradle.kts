tasks.create("writeVersionFile") {
    outputs.file("version.tex")
    outputs.upToDateWhen { false }

    file("version.tex").writeText(project.version.toString())
}

tasks.create<Exec>("buildManual") {
    group = "documentation"
    description = "Compile the manual's TeX file to PDF."

    dependsOn("writeVersionFile")

    commandLine(listOf("arara", "-l", "-v", "checkcites-manual.tex"))
    inputs.files("checkcites-manual.tex")
    outputs.files("checkcites-manual.pdf")
    outputs.upToDateWhen { false }
}
