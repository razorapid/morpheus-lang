# morpheus-lang java lib

Library:
- uses [SemVer](https://semver.org/) versioning scheme
- consists of:
  - lexer
  - parser
  - concrete syntax tree of the parsed language
  - abstract syntax tree of the parsed language
  - visitor interfaces to walk the trees
  - helper visitor implementations
    - CST to XML
    - CST to Graphivz graph
    - CST to AST
    - AST to Graphiz graph
    - *etc.*

## Development

You will need:
- JDK 17
- Gradle
- Lombok plugin and enabled annotation processing for your IDE

## Building

```shell
./gradlew clean build
```

## Usage examples

You can look up library tests to see how the lexer, parser and visitors are used.

