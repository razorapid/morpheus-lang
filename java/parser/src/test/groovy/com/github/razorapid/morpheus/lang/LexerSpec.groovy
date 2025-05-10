package com.github.razorapid.morpheus.lang

import com.github.razorapid.morpheus.lang.lexer.Lexer
import spock.lang.Specification
import spock.lang.Unroll

import static TokenType.*

class LexerSpec extends Specification {

    def "script cannot be null"() {
        when:
        new Lexer(null)

        then:
        thrown(NullPointerException)
    }

    def "EOL token on empty input"() {
        setup:
        def script = new Source("test_script.scr", "")
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 2
        result.get(0).type() == TOKEN_EOL
        result.get(1).type() == TOKEN_EOF
    }

    def "ignores whitespace"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.list().collect { it.type() } == expectedTokenType

        where:

        input    || expectedTokenType
        "     "  || [TOKEN_EOL, TOKEN_EOF]
        "   "    || [TOKEN_EOL, TOKEN_EOF]
        "\n\n\n" || [TOKEN_EOL, TOKEN_EOF]
        " \n \t" || [TOKEN_EOL, TOKEN_EOF]
    }

    def "ignores line comments"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 2
        result.get(0).type() == TOKEN_EOL
        result.get(1).type() == TOKEN_EOF

        where:

        input                 | _
        "//"                  | _
        "//some text"         | _
        "// some text"        | _
        "// (1234)"           | _
        "// (1234) test test" | _
    }

    def "ignores block comments"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 2
        result.get(0).type() == TOKEN_EOL
        result.get(1).type() == TOKEN_EOF

        where:

        input            | _
        "/**/"           | _
        "/***/"          | _
        "/****/"         | _
        "/* */"          | _
        "/*test*/"       | _
        "/* test */"     | _
        "/*te*st*/"      | _
        "/*te\nst*/"     | _
        "/*te\n\n\nst*/" | _
    }

    def "*/ illegal outside of block comment"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == TOKEN_ERROR
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input | _
        "*/"  | _
    }

    def "*/ outside of block comment skips scanning till end of line"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == TOKEN_ERROR
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input               | _
        "*/ level.test = 0" | _
    }

    def "scans brackets and braces"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == expectedTokenType
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input | expectedTokenType
        "("   | TOKEN_LEFT_BRACKET
        ")"   | TOKEN_RIGHT_BRACKET
        "["   | TOKEN_LEFT_SQUARE_BRACKET
        "]"   | TOKEN_RIGHT_SQUARE_BRACKET
        "{"   | TOKEN_LEFT_BRACES
        "}"   | TOKEN_RIGHT_BRACES
    }

    def "scans all other single character tokens"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == expectedTokenType
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input | expectedTokenType
        ";"   | TOKEN_SEMICOLON
        "\$"  | TOKEN_DOLLAR
        "~"   | TOKEN_COMPLEMENT
        "!"   | TOKEN_NOT
        "%"   | TOKEN_PERCENTAGE
        "*"   | TOKEN_MULTIPLY
        "/"   | TOKEN_DIVIDE
        "^"   | TOKEN_BITWISE_EXCL_OR
        "."   | TOKEN_PERIOD
        ":"   | TOKEN_COLON
        "="   | TOKEN_ASSIGNMENT
        "-"   | TOKEN_MINUS
        "+"   | TOKEN_PLUS
        "&"   | TOKEN_BITWISE_AND
        "|"   | TOKEN_BITWISE_OR
    }

    def "scans all other double character tokens"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == expectedTokenType
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input | expectedTokenType
        "::"  | TOKEN_DOUBLE_COLON
        "!="  | TOKEN_INEQUALITY
        "=="  | TOKEN_EQUALITY
        "<="  | TOKEN_LESS_THAN_OR_EQUAL
        ">="  | TOKEN_GREATER_THAN_OR_EQUAL
        "-="  | TOKEN_MINUS_EQUALS
        "+="  | TOKEN_PLUS_EQUALS
        "--"  | TOKEN_DEC
        "++"  | TOKEN_INC
        "&&"  | TOKEN_LOGICAL_AND
        "||"  | TOKEN_LOGICAL_OR
        " -"  | TOKEN_MINUS
        " +"  | TOKEN_PLUS
        " --" | TOKEN_DEC
        " ++" | TOKEN_INC
        " -=" | TOKEN_MINUS_EQUALS
        " +=" | TOKEN_PLUS_EQUALS
    }

    def "scans neg and pos character tokens"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 4
        result.get(0).type() == expectedTokenType
        result.get(1).type() == TOKEN_INTEGER
        result.get(2).type() == TOKEN_EOL
        result.get(3).type() == TOKEN_EOF

        where:

        input | expectedTokenType
        " -10"  | TOKEN_NEG
        " +10"  | TOKEN_POS
    }

    def "scans float numbers"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == expectedTokenType
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input    | expectedTokenType
        ".0"     | TOKEN_FLOAT
        ".1"     | TOKEN_FLOAT
        ".1E+1"  | TOKEN_FLOAT
        ".10E-2" | TOKEN_FLOAT
        "0.0"    | TOKEN_FLOAT
        "123.0"  | TOKEN_FLOAT
        "12.05"  | TOKEN_FLOAT
        "1.0E+1" | TOKEN_FLOAT
        "2.5E-2" | TOKEN_FLOAT
        "1.2E3"  | TOKEN_FLOAT
        "48E8"   | TOKEN_FLOAT
        "18E+8"  | TOKEN_FLOAT
        "09E-5"  | TOKEN_FLOAT
    }

    def "scans floats instead of indentifier"() {
        setup:
        def script = new Source("test_script.scr",
            """\
        println ("test") .1.2.3
        """.stripIndent())
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == expectedTokenTypes.size()
        result.list().collect { it.type() } == expectedTokenTypes

        where:

        expectedTokenTypes << [
            [
                TOKEN_IDENTIFIER,
                TOKEN_LEFT_BRACKET,
                TOKEN_STRING,
                TOKEN_RIGHT_BRACKET,
                TOKEN_PERIOD,
                TOKEN_IDENTIFIER,
                TOKEN_PERIOD,
                TOKEN_FLOAT,
                TOKEN_EOL,
                TOKEN_EOF
            ]
        ]
    }

    def "scans negative float numbers"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 4
        result.get(0).type() == TOKEN_NEG
        result.get(1).type() == expectedTokenType
        result.get(2).type() == TOKEN_EOL
        result.get(3).type() == TOKEN_EOF

        where:

        input   | expectedTokenType
        " -.0"    | TOKEN_FLOAT
        " -.1"    | TOKEN_FLOAT
        " -0.0"   | TOKEN_FLOAT
        " -123.0" | TOKEN_FLOAT
        " -12.05" | TOKEN_FLOAT
    }

    def "scans positive float numbers"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 4
        result.get(0).type() == TOKEN_POS
        result.get(1).type() == expectedTokenType
        result.get(2).type() == TOKEN_EOL
        result.get(3).type() == TOKEN_EOF

        where:

        input   | expectedTokenType
        " +.0"    | TOKEN_FLOAT
        " +.1"    | TOKEN_FLOAT
        " +0.0"   | TOKEN_FLOAT
        " +123.0" | TOKEN_FLOAT
        " +12.05" | TOKEN_FLOAT
    }

    def "scans float numbers followed by another character"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.get(0).type() == expectedTokenType[0]
        result.get(1).type() == expectedTokenType[1]

        where:

        input  | expectedTokenType
        ".5 "  | [TOKEN_FLOAT, TOKEN_EOL]
        ".5)"  | [TOKEN_FLOAT, TOKEN_RIGHT_BRACKET]
        ".5+"  | [TOKEN_FLOAT, TOKEN_PLUS]
        ".5\n" | [TOKEN_FLOAT, TOKEN_EOL]
        ".5\t" | [TOKEN_FLOAT, TOKEN_EOL]
    }

    def "scans integer numbers"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == expectedTokenType
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input      | expectedTokenType
        "0"        | TOKEN_INTEGER
        "1"        | TOKEN_INTEGER
        "2"        | TOKEN_INTEGER
        "10"       | TOKEN_INTEGER
        "123"      | TOKEN_INTEGER
        "3512"     | TOKEN_INTEGER
        "12764553" | TOKEN_INTEGER
    }

    def "scans negative integer numbers"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 4
        result.get(0).type() == TOKEN_NEG
        result.get(1).type() == expectedTokenType
        result.get(2).type() == TOKEN_EOL
        result.get(3).type() == TOKEN_EOF

        where:

        input        | expectedTokenType
        " -0"        | TOKEN_INTEGER
        " -1"        | TOKEN_INTEGER
        " -2"        | TOKEN_INTEGER
        " -10"       | TOKEN_INTEGER
        " -123"      | TOKEN_INTEGER
        " -3512"     | TOKEN_INTEGER
        " -12764553" | TOKEN_INTEGER
    }

    def "scans positive integer numbers"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 4
        result.get(0).type() == TOKEN_POS
        result.get(1).type() == expectedTokenType
        result.get(2).type() == TOKEN_EOL
        result.get(3).type() == TOKEN_EOF

        where:

        input        | expectedTokenType
        " +0"        | TOKEN_INTEGER
        " +1"        | TOKEN_INTEGER
        " +2"        | TOKEN_INTEGER
        " +10"       | TOKEN_INTEGER
        " +123"      | TOKEN_INTEGER
        " +3512"     | TOKEN_INTEGER
        " +12764553" | TOKEN_INTEGER
    }

    def "scans strings"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == expectedTokenType
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input                       | expectedTokenType
        '"0"'                       | TOKEN_STRING
        '"1"'                       | TOKEN_STRING
        '"this is some text"'       | TOKEN_STRING
        '""'                        | TOKEN_STRING
        '" "'                       | TOKEN_STRING
        '"string\\nwith\\nescapes"' | TOKEN_STRING
        '"string\\"with\\"escapes"' | TOKEN_STRING
    }

    def "doesn't scan strings"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() != notExpectedTokenType
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input   | notExpectedTokenType
        '""a'   | TOKEN_STRING
        '"x"a'  | TOKEN_STRING
        '"this' | TOKEN_STRING
        '"\\"'  | TOKEN_STRING
        '"'     | TOKEN_STRING
    }

    def "scans keywords"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == expectedTokenType
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input            | expectedTokenType
        "case"           | TOKEN_CASE
        "if"             | TOKEN_IF
        "else"           | TOKEN_ELSE
        "while"          | TOKEN_WHILE
        "for"            | TOKEN_FOR
        "try"            | TOKEN_TRY
        "catch"          | TOKEN_CATCH
        "switch"         | TOKEN_SWITCH
        "break"          | TOKEN_BREAK
        "continue"       | TOKEN_CONTINUE
        "NULL"           | TOKEN_NULL
        "NIL"            | TOKEN_NIL
        "size"           | TOKEN_SIZE
        "end"            | TOKEN_END
        "makeArray"      | TOKEN_MAKEARRAY
        "makearray"      | TOKEN_MAKEARRAY
        "endArray"       | TOKEN_ENDARRAY
        "endarray"       | TOKEN_ENDARRAY
        "ifequal"        | TOKEN_EQUALITY
        "ifstrequal"     | TOKEN_EQUALITY
        "ifnotequal"     | TOKEN_INEQUALITY
        "ifstrnotequal"  | TOKEN_INEQUALITY
        "ifless"         | TOKEN_LESS_THAN
        "iflessequal"    | TOKEN_LESS_THAN_OR_EQUAL
        "ifgreater"      | TOKEN_GREATER_THAN
        "ifgreaterequal" | TOKEN_GREATER_THAN_OR_EQUAL
        "append"         | TOKEN_PLUS_EQUALS
        "appendint"      | TOKEN_PLUS_EQUALS
        "appendfloat"    | TOKEN_PLUS_EQUALS
    }

    def "scans keyword followed by other characters"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.list().collect { it.type() } == expectedTokenType

        where:

        input             || expectedTokenType
        "continue"        || [TOKEN_CONTINUE, TOKEN_EOL, TOKEN_EOF]
        "continuea"       || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "continue1"       || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "continue;"       || [TOKEN_CONTINUE, TOKEN_SEMICOLON, TOKEN_EOL, TOKEN_EOF]
        "continue\$"      || [TOKEN_CONTINUE, TOKEN_DOLLAR, TOKEN_EOL, TOKEN_EOF]
        "continue~"       || [TOKEN_CONTINUE, TOKEN_COMPLEMENT, TOKEN_EOL, TOKEN_EOF]
        "continue!"       || [TOKEN_CONTINUE, TOKEN_NOT, TOKEN_EOL, TOKEN_EOF]
        "continue%"       || [TOKEN_CONTINUE, TOKEN_PERCENTAGE, TOKEN_EOL, TOKEN_EOF]
        "continue*"       || [TOKEN_CONTINUE, TOKEN_MULTIPLY, TOKEN_EOL, TOKEN_EOF]
        "continue/"       || [TOKEN_CONTINUE, TOKEN_DIVIDE, TOKEN_EOL, TOKEN_EOF]
        "continue^"       || [TOKEN_CONTINUE, TOKEN_BITWISE_EXCL_OR, TOKEN_EOL, TOKEN_EOF]
        "continue."       || [TOKEN_CONTINUE, TOKEN_PERIOD, TOKEN_EOL, TOKEN_EOF]
        "continue:"       || [TOKEN_CONTINUE, TOKEN_COLON, TOKEN_EOL, TOKEN_EOF]
        "continue="       || [TOKEN_CONTINUE, TOKEN_ASSIGNMENT, TOKEN_EOL, TOKEN_EOF]
        "continue-"       || [TOKEN_CONTINUE, TOKEN_MINUS, TOKEN_EOL, TOKEN_EOF]
        "continue+"       || [TOKEN_CONTINUE, TOKEN_PLUS, TOKEN_EOL, TOKEN_EOF]
        "continue&"       || [TOKEN_CONTINUE, TOKEN_BITWISE_AND, TOKEN_EOL, TOKEN_EOF]
        "continue|"       || [TOKEN_CONTINUE, TOKEN_BITWISE_OR, TOKEN_EOL, TOKEN_EOF]
        "continue["       || [TOKEN_CONTINUE, TOKEN_LEFT_SQUARE_BRACKET, TOKEN_EOL, TOKEN_EOF]
        "continue]"       || [TOKEN_CONTINUE, TOKEN_RIGHT_SQUARE_BRACKET, TOKEN_EOL, TOKEN_EOF]
        "continue("       || [TOKEN_CONTINUE, TOKEN_LEFT_BRACKET, TOKEN_EOL, TOKEN_EOF]
        "continue)"       || [TOKEN_CONTINUE, TOKEN_RIGHT_BRACKET, TOKEN_EOL, TOKEN_EOF]
        "continue{"       || [TOKEN_CONTINUE, TOKEN_LEFT_BRACES, TOKEN_EOL, TOKEN_EOF]
        "continue}"       || [TOKEN_CONTINUE, TOKEN_RIGHT_BRACES, TOKEN_EOL, TOKEN_EOF]
        "continue\\"      || [TOKEN_CONTINUE, TOKEN_EOF]
        "continue,"       || [TOKEN_CONTINUE, TOKEN_EOF]
        "continue@"       || [TOKEN_CONTINUE, TOKEN_EOF]
        "continue#"       || [TOKEN_CONTINUE, TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "continue?"       || [TOKEN_CONTINUE, TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "continue::"      || [TOKEN_CONTINUE, TOKEN_DOUBLE_COLON, TOKEN_EOL, TOKEN_EOF]
        "continue!="      || [TOKEN_CONTINUE, TOKEN_INEQUALITY, TOKEN_EOL, TOKEN_EOF]
        "continue=="      || [TOKEN_CONTINUE, TOKEN_EQUALITY, TOKEN_EOL, TOKEN_EOF]
        "continue<="      || [TOKEN_CONTINUE, TOKEN_LESS_THAN_OR_EQUAL, TOKEN_EOL, TOKEN_EOF]
        "continue>="      || [TOKEN_CONTINUE, TOKEN_GREATER_THAN_OR_EQUAL, TOKEN_EOL, TOKEN_EOF]
        "continue-="      || [TOKEN_CONTINUE, TOKEN_MINUS_EQUALS, TOKEN_EOL, TOKEN_EOF]
        "continue+="      || [TOKEN_CONTINUE, TOKEN_PLUS_EQUALS, TOKEN_EOL, TOKEN_EOF]
        "continue--"      || [TOKEN_CONTINUE, TOKEN_DEC, TOKEN_EOL, TOKEN_EOF]
        "continue++"      || [TOKEN_CONTINUE, TOKEN_INC, TOKEN_EOL, TOKEN_EOF]
        "continue&&"      || [TOKEN_CONTINUE, TOKEN_LOGICAL_AND, TOKEN_EOL, TOKEN_EOF]
        "continue||"      || [TOKEN_CONTINUE, TOKEN_LOGICAL_OR, TOKEN_EOL, TOKEN_EOF]
        "continue\$a"     || [TOKEN_CONTINUE, TOKEN_DOLLAR, TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "continue.a"      || [TOKEN_CONTINUE, TOKEN_PERIOD, TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "ifequalx"        || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "ifstrequalx"     || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "ifnotequalx"     || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "ifstrnotequalx"  || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "iflessx"         || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "iflessequalx"    || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "ifgreaterx"      || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "ifgreaterequalx" || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "appendx"         || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "appendintx"      || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "appendfloatx"    || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
    }

    def "doesn't scan identifiers as keywords"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.list().collect { it.type() } == expectedTokenType

        where:

        input                     || expectedTokenType
        "end1"                    || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "enda"                    || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "end_tank_destroyed"      || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "continue_tank_destroyed" || [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]

    }

    def "scans identifiers with dots inside expressions"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.list().collect { it.type() } == expectedTokenType

        where:

        input                    || expectedTokenType
        "\$(sdkfz.gunner) test"  || [TOKEN_DOLLAR, TOKEN_LEFT_BRACKET, TOKEN_IDENTIFIER, TOKEN_RIGHT_BRACKET, TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\$enemy[local.i].set--" || [TOKEN_DOLLAR, TOKEN_IDENTIFIER, TOKEN_LEFT_SQUARE_BRACKET, TOKEN_LISTENER, TOKEN_PERIOD, TOKEN_IDENTIFIER, TOKEN_RIGHT_SQUARE_BRACKET, TOKEN_PERIOD, TOKEN_IDENTIFIER, TOKEN_DEC, TOKEN_EOL, TOKEN_EOF]
        "\$enemy[local.i]abcd.set--" || [TOKEN_DOLLAR, TOKEN_IDENTIFIER, TOKEN_LEFT_SQUARE_BRACKET, TOKEN_LISTENER, TOKEN_PERIOD, TOKEN_IDENTIFIER, TOKEN_RIGHT_SQUARE_BRACKET, TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\$santatarget .collisionent=\$opelcollision_mask" || [TOKEN_DOLLAR, TOKEN_IDENTIFIER, TOKEN_PERIOD, TOKEN_IDENTIFIER, TOKEN_ASSIGNMENT, TOKEN_DOLLAR, TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
    }

    def "scans listeners"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == expectedTokenType
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input   | expectedTokenType
        "game"  | TOKEN_LISTENER
        "level" | TOKEN_LISTENER
        "local" | TOKEN_LISTENER
        "parm"  | TOKEN_LISTENER
        "self"  | TOKEN_LISTENER
        "group" | TOKEN_LISTENER
    }

    def "scans identifiers"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 3
        result.get(0).type() == TOKEN_IDENTIFIER
        result.get(1).type() == TOKEN_EOL
        result.get(2).type() == TOKEN_EOF

        where:

        input         | _
        "something"   | _
        "\"something" | _
        "#something"  | _
        "'something"  | _
        ",something"  | _
        "0something"  | _
        "1something"  | _
        "2something"  | _
        "3something"  | _
        "4something"  | _
        "5something"  | _
        "6something"  | _
        "7something"  | _
        "8something"  | _
        "9something"  | _
        "?something"  | _
        "@something"  | _
        "Asomething"  | _
        "Bsomething"  | _
        "Csomething"  | _
        "Dsomething"  | _
        "Esomething"  | _
        "Fsomething"  | _
        "Gsomething"  | _
        "Hsomething"  | _
        "Isomething"  | _
        "Jsomething"  | _
        "Ksomething"  | _
        "Lsomething"  | _
        "Msomething"  | _
        "Nsomething"  | _
        "Osomething"  | _
        "Psomething"  | _
        "Qsomething"  | _
        "Rsomething"  | _
        "Ssomething"  | _
        "Tsomething"  | _
        "Usomething"  | _
        "Vsomething"  | _
        "Wsomething"  | _
        "Xsomething"  | _
        "Ysomething"  | _
        "Zsomething"  | _
        "something"   | _
        "_something"  | _
        "`something"  | _
        "asomething"  | _
        "bsomething"  | _
        "csomething"  | _
        "dsomething"  | _
        "esomething"  | _
        "fsomething"  | _
        "gsomething"  | _
        "hsomething"  | _
        "isomething"  | _
        "jsomething"  | _
        "ksomething"  | _
        "lsomething"  | _
        "msomething"  | _
        "nsomething"  | _
        "osomething"  | _
        "psomething"  | _
        "qsomething"  | _
        "rsomething"  | _
        "ssomething"  | _
        "tsomething"  | _
        "usomething"  | _
        "vsomething"  | _
        "wsomething"  | _
        "xsomething"  | _
        "ysomething"  | _
        "zsomething"  | _
        "s!omething"  | _
        "s\"omething" | _
        "s#omething"  | _
        "s\$omething" | _
        "s%omething"  | _
        "s&omething"  | _
        "s'omething"  | _
        "something"   | _
        "s*omething"  | _
        "s+omething"  | _
        "s-omething"  | _
        "s.omething"  | _
        "s/omething"  | _
        "s0omething"  | _
        "s1omething"  | _
        "s2omething"  | _
        "s3omething"  | _
        "s4omething"  | _
        "s5omething"  | _
        "s6omething"  | _
        "s7omething"  | _
        "s8omething"  | _
        "s9omething"  | _
        "s<omething"  | _
        "s=omething"  | _
        "s>omething"  | _
        "s?omething"  | _
        "s@omething"  | _
        "sAomething"  | _
        "sBomething"  | _
        "sComething"  | _
        "sDomething"  | _
        "sEomething"  | _
        "sFomething"  | _
        "sGomething"  | _
        "sHomething"  | _
        "sIomething"  | _
        "sJomething"  | _
        "sKomething"  | _
        "sLomething"  | _
        "sMomething"  | _
        "sNomething"  | _
        "sOomething"  | _
        "sPomething"  | _
        "sQomething"  | _
        "sRomething"  | _
        "sSomething"  | _
        "sTomething"  | _
        "sUomething"  | _
        "sVomething"  | _
        "sWomething"  | _
        "sXomething"  | _
        "sYomething"  | _
        "sZomething"  | _
        "something"   | _
        "s^omething"  | _
        "s_omething"  | _
        "s`omething"  | _
        "saomething"  | _
        "sbomething"  | _
        "scomething"  | _
        "sdomething"  | _
        "seomething"  | _
        "sfomething"  | _
        "sgomething"  | _
        "shomething"  | _
        "siomething"  | _
        "sjomething"  | _
        "skomething"  | _
        "slomething"  | _
        "smomething"  | _
        "snomething"  | _
        "soomething"  | _
        "spomething"  | _
        "sqomething"  | _
        "sromething"  | _
        "ssomething"  | _
        "stomething"  | _
        "suomething"  | _
        "svomething"  | _
        "swomething"  | _
        "sxomething"  | _
        "syomething"  | _
        "szomething"  | _
        "s|omething"  | _
        "s~omething"  | _
        "1.2.3"       | _
    }

    def "comma separates identifiers"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 4
        result.get(0).type() == TOKEN_IDENTIFIER
        result.get(0).lexeme() == expectedLexemes[0]
        result.get(1).type() == TOKEN_IDENTIFIER
        result.get(1).lexeme() == expectedLexemes[1]
        result.get(2).type() == TOKEN_EOL
        result.get(3).type() == TOKEN_EOF

        where:
        input          | expectedLexemes
        "s,omething"   | ["s", "omething"]
        "s\\,omething" | ["s\\", "omething"]
    }

    def "scans escaped identifiers"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.list().collect { it.type() } == expectedTokenTypes

        where:
        input   | expectedTokenTypes
        "\\ "   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\\t"  | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\("   | [TOKEN_IDENTIFIER, TOKEN_LEFT_BRACKET, TOKEN_EOL, TOKEN_EOF]
        "\\)"   | [TOKEN_IDENTIFIER, TOKEN_RIGHT_BRACKET, TOKEN_EOL, TOKEN_EOF]
        "\\["   | [TOKEN_IDENTIFIER, TOKEN_LEFT_SQUARE_BRACKET, TOKEN_EOL, TOKEN_EOF]
        "\\]"   | [TOKEN_IDENTIFIER, TOKEN_RIGHT_SQUARE_BRACKET, TOKEN_EOL, TOKEN_EOF]
        "\\{"   | [TOKEN_IDENTIFIER, TOKEN_LEFT_BRACES, TOKEN_EOL, TOKEN_EOF]
        "\\}"   | [TOKEN_IDENTIFIER, TOKEN_RIGHT_BRACES, TOKEN_EOL, TOKEN_EOF]
        "\\:"   | [TOKEN_IDENTIFIER, TOKEN_COLON, TOKEN_EOL, TOKEN_EOF]
        "\\;"   | [TOKEN_IDENTIFIER, TOKEN_SEMICOLON, TOKEN_EOL, TOKEN_EOF]
        "\\,"   | [TOKEN_IDENTIFIER, TOKEN_EOF]
        "\\!"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\%"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\&"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\*"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\+"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\-"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\."   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\/"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\\\"  | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\|"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\<"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\>"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\="   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\^"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\~"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\\r\r"| [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\\ra" | [TOKEN_IDENTIFIER, TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\a"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\A"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\@"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\1"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\2"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\\""  | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
        "\\_"   | [TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
    }

    def "scans listener variable as float token instead of identifier when it contains only digits"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.list().collect { it.type() } == expectedTokenType

        where:

        input      || expectedTokenType
        "local.25" || [TOKEN_LISTENER, TOKEN_FLOAT, TOKEN_EOL, TOKEN_EOF]
    }

    def "doesn't scan empty targetname identifier"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.list().collect { it.type() } == expectedTokenType

        where:

        input     || expectedTokenType
        "\$."     || [TOKEN_DOLLAR, TOKEN_PERIOD, TOKEN_EOL, TOKEN_EOF]
        "\$.test" || [TOKEN_DOLLAR, TOKEN_PERIOD, TOKEN_IDENTIFIER, TOKEN_EOL, TOKEN_EOF]
    }

    def "scans double character positive and negative tokens"() {
        setup:
        def script = new Source("test_script.scr", input)
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == 4
        result.get(0).type() == expectedTokenType
        result.get(1).type() == TOKEN_INTEGER
        result.get(2).type() == TOKEN_EOL
        result.get(3).type() == TOKEN_EOF

        where:

        input | expectedTokenType
        " -1"  | TOKEN_NEG
        " +1"  | TOKEN_POS
    }

    def "scans basic script"() {
        setup:
        def script = new Source("test_script.scr",
        """\
        main:
          local.i = "some text";
          println local.i;
        end
        """.stripIndent())
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()

        then:
        result.size() == expectedTokenTypes.size()
        result.list().collect { it.type() } == expectedTokenTypes

        where:

        expectedTokenTypes << [
                [
                        TOKEN_IDENTIFIER,
                        TOKEN_COLON,
                        TOKEN_EOL,
                        TOKEN_LISTENER,
                        TOKEN_PERIOD,
                        TOKEN_IDENTIFIER,
                        TOKEN_ASSIGNMENT,
                        TOKEN_STRING,
                        TOKEN_SEMICOLON,
                        TOKEN_EOL,
                        TOKEN_IDENTIFIER,
                        TOKEN_LISTENER,
                        TOKEN_PERIOD,
                        TOKEN_IDENTIFIER,
                        TOKEN_SEMICOLON,
                        TOKEN_EOL,
                        TOKEN_END,
                        TOKEN_EOL,
                        TOKEN_EOF
                ]
        ]
    }

    @Unroll
    def "parses #scriptName script to #expectedTokenTypes"() {
        setup:
        def script = new Source(scriptName, loadScript(scriptName))
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()
        def resultTokenTypes = result.list().collect { it.type() }

        then:
        resultTokenTypes == loadTokensTypes(expectedTokenTypes)

        where:

        scriptName   || expectedTokenTypes
        "1_test.scr" || "1_test.tokens"
        "2_test.scr" || "2_test.tokens"
        "3_test.scr" || "3_test.tokens"
        "4_test.scr" || "4_test.tokens"
        "5_test.scr" || "5_test.tokens"
        "6_test.scr" || "6_test.tokens"
        "7_test.scr" || "7_test.tokens"
        "8_test.scr" || "8_test.tokens"

    }

    def "properly counts token column and line numbers"() {
        setup:
        def script = new Source(scriptName, loadScript(scriptName))
        def lexer = new Lexer(script)

        when:
        def result = lexer.scan()
        def resultTokenTypes = result.list()

        then:
        def expected = loadTokens(expectedTokenTypes)
        resultTokenTypes.eachWithIndex { Token entry, int i ->
            assert entry.type() == expected[i].type()
            assert entry.col() == expected[i].col()
            assert entry.line() == expected[i].line()
        }
        where:

        scriptName   || expectedTokenTypes
        "line_and_col_test.scr" || "line_and_col_test.tokens"
    }

    private String loadScript(String scriptFilename) {
        return this.class.getResource("/lexer/" + scriptFilename).text
    }

    private List<TokenType> loadTokensTypes(String tokensFilename) {
        def tokens = this.class.getResource("/lexer/" + tokensFilename).text.split("\n").toList()
        return tokens.collect { valueOf(it) }
    }

    private List<Token> loadTokens(String tokensFilename) {
        def tokens = this.class.getResource("/lexer/" + tokensFilename).text.split("\n").toList()
        return tokens.collect {entry ->
            def parts = entry.split("\\|")
            return Token.of(valueOf(parts[0]), parts[1], -1, Long.parseLong(parts[2]), Long.parseLong(parts[3]))
        }
    }
}
