# `checkcites.lua`

## License

This script is licensed under the LaTeX Project Public License.
If you want to support LaTeX development by a donation, the best
way to do this is donating to the TeX Users Group.

## About

`checkcites` is a Lua script written for the sole purpose of detecting
unused or undefined references from both LaTeX auxiliary or bibliography
files. We use the term *unused reference* to refer to the reference
present the bibliography file - with the `.bib` extension - but not
cited in the `.tex` file. The term *undefined reference* is exactly the
opposite, i.e, the item cited in the `.tex` file, but not present in the
`.bib` file.

The original idea came from a question posted in the TeX community at
Stack Exchange about how to check which bibliography entries were not
used. We decided to write a script to check references. We opted for
Lua, since it's a very straightforward language and it has an
interpreter available on every modern TeX distribution.

# Usage

The script is pretty simple to use. The only requirement is a recent
TeX distribution, such as TeX Live.

`checkcites` uses the generated auxiliary files to start the analysis.
From version 2.0 on, the scripts supports two backends:

## `bibtex`

Default behavior, the script checks `.aux` files looking for citations,
in the form of `\citation{a}`. For every `\citation` line found, `checkcites`
will extract the citations and add them to a table, even for multiple
citations separated by commas, like `\citation{a,b,c}`. The citation
table contains no duplicate values. At the same time checkcites also
looks for bibliography data, in the form of `\bibdata{a}`. Similarly,
for every `\bibdata` line found, the script will extract the bibliography
data and add them to a table, even if they are separated by commas, like
`\bibdata{d,e,f}`. Again, no duplicate values are allowed. Stick with this
backend if you are using BibTeX or BibLaTeX with the `backend=bibtex`
package option.

## `biber`

With this backend, the script checks `.bcf` files (which are XML-based)
looking for citations, in the form of `bcf:citekey` tags. For every tag
found, `checkcites` will extract the corresponding values and add them to
a table. The citation table contains no duplicate values. At the same
time `checkcites` also looks for bibliography data, in the form of
`bcf:datasource` tags. Similarly, for every tag found, the script will
extract the bibliography data and add them to a table. Again, no duplicate
values are allowed. Stick with this backend if you are using BibLaTeX with
the default options or with the `backend=biber` option explicitly set.
It is important to note, however, that the `glob=true` option is not
supported yet.

## Command line

Open a terminal and run `checkcites`:

```bash
$ checkcites
```

When you run `checkcites` without providing any argument to it, the a message
error will appear. Do not panic! Try again with the `--help` flag:

```bash
$ checkcites --help
```

If you are using BibTeX, simply provide the auxiliary file - the one with
the `.aux` extension - which is generated when you compile your main `.tex`
file. For example, if your main document is named `foo.tex`, you probably
have a `foo.aux` file too. Then simply invoke

```bash
$ checkcites foo.aux
```

`checkcites` allows an additional argument that will tell it how to
behave. For example

```bash
$ checkcites --unused foo.aux
```

will make the script only look for unused references in your `.bib`
file. The argument order doesn't matter, you can also run

```bash
$ checkcites foo.aux --unused
```

and get the same behaviour. Similarly, you can use

```bash
$ checkcites --undefined foo.aux
```

to make the script only look for undefined references in your
`.tex` file. If you want `checkcites` to look for both unused and
undefined references, go with

```bash
$ checkcites --all foo.aux
```

If no special argument is provided, `--all` is set as default.

If you are using BibLaTeX, we need to inspect `.bcf` files instead. For
example, if your main document is named `foo.tex`, you probably have a
`foo.bcf` file too. Then invoke

```bash
$ checkcites foo.aux --backend biber
```

Note the `--backend` flag used for BibLaTeX support. We can even omit the
file extension, the script will automatically assign one based on the
current backend.

That is it, folks!
