# morpheus-lang

This repository contains **Morpheus** scripting language documentation, grammar specification and
a reference programming library enabling developers to process scripts written using **Morpheus**.

- Language documentation is available in [morpheus.md](morpheus.md) file.
- The grammar is available in [morpheus.xbnf](morpheus.xbnf) file.
- Reference implementation is a recursive descent parser handwritten in Java
  - it produces a concrete syntax tree (parse tree) that can be later simplified into an abstract syntax tree.

## Background

**Morpheus** scripting language is a programming language used in computer games built with a
modified idTech 3 engine (Quake 3) and later versions.
It was provided as a part of ÜberTools Game Development Kit by Ritual Entertainment.
Each version of ÜberTools GDK came with a different Morpheus language version and introduced
new language syntax and features.

Version of the language implemented in this repository is the one used in Medal of Honor franchise,
specifically in titles:
- Medal of Honor: Allied Assault
- Medal of Honor: Spearhead
- Medal of Honor: Breakthrough

Different versions or flavors can be found in other games, ie.:
- American McGee’s Alice
- SiN
- Heavy Metal: F.A.K.K. 2
- Star Trek: Elite Force II
- Call of Duty series
- Other Medal of Honor installments
- ...

## Project structure

- morpheus-lang:
  - README.md - you are reading this now
  - morpheus.md - language documentation
  - morpheus.xbnf - grammar specification
  - java/ - reference parser implementation in Java
  - \<language\>/ - implementation in \<language\>

Each \<language\> directory has its own README file describing how to build and use the library
in that language.

## Project goals

**Morpheus** is a simple language, yet it comes with a lot of quirks - enough to make it hard to implement
them all 1:1 using parser generators (i.e. ANTLR, flex/bison etc.). While it's possible, it takes a lot
of time to master these tools to replicate exact behavior found in games where it's used originally.

Because of that and author's personal interest in programming languages, the project also serves as a learning
experience in writing lexers, parsers, compilers etc.

These are the main reasons for the reference parser implementation to be a handwritten recursive descent parser.
This is why the reference library source code focuses on:
- readability over performance
  - functions try to follow grammar production rules as close as possible
  - parser uses look-ahead and backtracking in few places
  - performance improvements are welcome as long as they don't hinder readability
- ease of maintenance
- ease of portability
  - handwritten parser can be easily ported to other languages (even to Morpheus itself),
    which would otherwise require
    a parser generator for given target language and creation of grammar that fits the tool

Libraries in this repository can be used for many things:
- linters
- formatters
- syntax highlighting
- IDE language support (LSP backend etc.)
- visualisations
- *etc.*

## Contributing

- [License](LICENSE.md)
- [Guide](.github/CONTRIBUTING.md)
- [Code of Conduct](.github/CODE_OF_CONDUCT.md)
- Please follow [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/):
  - types
    - feat - feature
    - fix - bugfix
    - test - implementation of new test cases
    - refactor - code changes that don't change behavior
    - perf - performance improvements
    - build - changes in build scripts
    - ci - changes in CI
    - doc - documentation improvements
    - chore - other work
  - scopes
    - \<language\> - changes for library in given implementation language
    - lexer - changes in the lexer
    - parser - changes in the parser
    - cst - changes in the concrete syntax tree
    - ast - changes in the abstract syntax tree
  - examples
    - feat(java,parser): support arithmetic positive unary operator

Having knowledge on crafting good commit messages is also welcome
(you can read about that in [this article](https://chris.beams.io/posts/git-commit/))

When creating documentation, please try to follow markdown good practice guidelines that can be found
[here](https://www.markdownguide.org)
