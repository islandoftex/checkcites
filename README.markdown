# checkcites.lua

> **Current stable version:** 1.0g from March 7, 2012

## The script

`checkcites.lua` is a humble Lua script written for the sole purpose of detecting unused or undefined references from LaTeX auxiliary (`.aux`) or bibliography (`.bib`) files.

The idea came from [this answer of mine](http://tex.stackexchange.com/a/43288/3094) in the [TeX.sx](http://tex.stackexchange.com/) community. My good friend [Enrico Gregorio](http://tex.stackexchange.com/users/4427/egreg) encouraged me and gave me great ideas to write this script, so we came up with `checkcites.lua`!

## How it works

First of all, we analyze the `.aux` file and extract both citations and bibliography. Let's call the citations set `A`.

Now, we extract all entries from the bibliography files we found referenced in the previous step. Let's call the bibliography entries set `B`.

Now we have both sets, let's do the math! If we want to get all undefined references in our `.tex` file, we simply go with:

![Undefined references](http://latex.codecogs.com/png.latex?%5CLARGE%20A%20-%20B%20%3D%20%5C%7B%20x%20%3A%20x%20%5Cin%20A%2C%20x%20%5Cnotin%20B%20%5C%7D)

For unused references in our `.bib` file(s), we go with:

![Unused references](http://latex.codecogs.com/png.latex?%5CLARGE%20B%20-%20A%20%3D%20%5C%7B%20x%20%3A%20x%20%5Cin%20B%2C%20x%20%5Cnotin%20A%20%5C%7D)

## Usage

The script is pretty simple to use. The only requirement is a recent TeX distribution, such as [TeX Live](http://www.tug.org/texlive/). Then run `checkcites.lua`:

    $ texlua checkcites.lua

It will print the script usage. The only required argument is the `.aux` file which is generated when you compile your `.tex` file. If your main document is named `foo.tex`, you will have a `foo.aux` file too. To run the script on that file, go with

    $ texlua checkcites.lua foo.aux

`checkcites.lua` allows an additional argument that will tell it how to behave. For example

    $ texlua checkcites.lua --unused foo.aux

will make the script only look for unused references in your `.bib` file. The argument order doesn't matter, you can also run

    $ texlua checkcites.lua foo.aux --unused

and get the same behaviour. Similarly, you can use

    $ texlua checkcites.lua --undefined foo.aux

to make the script only look for undefined references in your `.tex` file. If you want `checkcites.lua` to look for both unused and undefined references, go with

    $ texlua checkcites.lua --all foo.aux

If no special argument is provided, `--all` is set as default.

## License

This script is licensed under the [LaTeX Project Public License](http://www.latex-project.org/lppl/). If you want to support LaTeX development by a donation, the best way to do this is donating to the [TeX Users Group](http://www.tug.org/).

## The authors

You can reach us through the following links:

+ [Enrico Gregorio](http://profs.scienze.univr.it/~gregorio/)
+ [Paulo Roberto Massa Cereda](http://cereda.users.sourceforge.net/)

You can also find us in the [TeX, LaTeX and Friends](http://chat.stackexchange.com/rooms/41/tex-latex-and-friends) chatroom of the [TeX.sx](http://tex.stackexchange.com/) community.

## Changelog

### 1.0g

First public release. Yay!

