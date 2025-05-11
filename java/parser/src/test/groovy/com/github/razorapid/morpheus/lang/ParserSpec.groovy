package com.github.razorapid.morpheus.lang

import com.github.razorapid.morpheus.lang.lexer.Lexer
import com.github.razorapid.morpheus.lang.parser.Parser
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.engine.GraphvizV8Engine
import com.github.razorapid.morpheus.lang.ast.visitors.VisualAstGraphVisitor
import com.github.razorapid.morpheus.lang.cst.visitors.VisualGraphVisitor
import com.github.razorapid.morpheus.lang.cst.visitors.XmlPrinterVisitor
import spock.lang.Specification

import java.nio.file.Files

import static TokenType.TOKEN_SEMICOLON
import static org.xmlunit.assertj3.XmlAssert.assertThat

class ParserSpec extends Specification {

    def "parses empty program"() {
        setup:
        def source = new Source("test_script.scr", "")
        def parser = new Parser(source, tokens)

        when:
        def cst = parser.parse()

        then:
        cst != null

        where:
        tokens          || _
        Tokens.create() || _
    }

    def "parses empty statement"() {
        setup:
        def source = new Source("test_script.scr", ";")
        def parser = new Parser(source, Tokens.of(tokens))

        when:
        def cst = parser.parse()

        then:
        cst != null

        where:
        tokens               || _
        [t(TOKEN_SEMICOLON)] || _
    }

    def "parses simple statements"() {
        setup:
        def source = new Source("test_script.scr", script)
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst))

        where:
        script     | serialize | visualize || expectMatchedCst
        ";"        | false     | false     || '0_test'
        "break"    | false     | false     || '1_test'
        "continue" | false     | false     || '2_test'
    }

    def "parses decrement statements"() {
        setup:
        def source = new Source("test_script.scr", script)
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst))

        where:
        script        | serialize | visualize || expectMatchedCst
        "NIL--"       | false     | false     || '3_test'
        "NULL--"      | false     | false     || '4_test'
        "local--"     | false     | false     || '5_test'
        "(5 5 5)--"   | false     | false     || '6_test'
        "5.0--"       | false     | false     || '7_test'
        "5--"         | false     | false     || '8_test'
        "\"abcd\"--"  | false     | false     || '9_test'
        "\$players--" | false     | false     || '10_test'
        "local.i--"   | false     | false     || '11_test'
    }

    def "parses increment statements"() {
        setup:
        def source = new Source("test_script.scr", script)
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst))

        where:
        script        | serialize | visualize || expectMatchedCst
        "NIL++"       | false     | false     || '12_test'
        "NULL++"      | false     | false     || '13_test'
        "local++"     | false     | false     || '14_test'
        "(5 5 5)++"   | false     | false     || '15_test'
        "5.0++"       | false     | false     || '16_test'
        "5++"         | false     | false     || '17_test'
        "\"abcd\"++"  | false     | false     || '18_test'
        "\$players++" | false     | false     || '19_test'
        "local.i++"   | false     | false     || '20_test'
    }

    def "parses compound statements"() {
        setup:
        def source = new Source("test_script.scr", script)
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst))

        where:
        script                                 | serialize | visualize || expectMatchedCst
        "{}"                                   | false     | false     || '21_test'
        "{ println }"                          | false     | false     || '22_test'
        "{ local.i++; local.j++; }"            | false     | false     || '23_test'
        "{\n\tlocal.i++\n\tprintln local.i\n}" | false     | false     || '24_test'
    }

    def "parses label statements"() {
        setup:
        def source = new Source("test_script.scr", script)
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst))

        where:
        script                          | serialize | visualize || expectMatchedCst
        "some_label:"                   | false     | false     || '25_test'
        "some_label local.i:"           | false     | false     || '26_test'
        "case some_label:"              | false     | false     || '27_test'
        "case \"some_string\":"         | false     | false     || '28_test'
        "case 0:"                       | false     | false     || '29_test'
        "case 1:"                       | false     | false     || '30_test'
        "case some_label local.i:"      | false     | false     || '31_test'
        "case \"some_string\" local.i:" | false     | false     || '32_test'
        "case 0 local.i:"               | false     | false     || '33_test'
        "case 1 local.i:"               | false     | false     || '34_test'
    }

    def "parses selection statements"() {
        setup:
        def source = new Source("test_script.scr", script)
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst))

        where:
        script                                        | serialize | visualize || expectMatchedCst
        "if true println \"abcd\""                    | false     | false     || '35_test'
        "if level println \"level\""                  | false     | false     || '36_test'
        "if local.i local.i++"                        | false     | false     || '37_test'
        "if local.i local.i++ else println \"error\"" | false     | false     || '38_test'
        "if local.i local.i++ else println \"error\"" | false     | false     || '39_test'
        "if 1::1 println \"test\""                    | false     | false     || '40_test'

        "switch true {}"                              | false     | false     || '41_test'
        "switch level {}"                             | false     | false     || '42_test'
        "switch local.i {}"                           | false     | false     || '43_test'
        "switch local.i { case 0: println \"test\" }" | false     | false     || '44_test'
    }

    def "parses iteration statements"() {
        setup:
        def source = new Source("test_script.scr", script)
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst))

        where:
        script                                                          | serialize | visualize || expectMatchedCst
        "while true local.i++"                                          | false     | false     || '45_test'
        "for (local.i = 0; local.i < 2; local.i++) { println local.i }" | false     | false     || '46_test'
    }

    def "parses try catch statements"() {
        setup:
        def source = new Source("test_script.scr", script)
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst))

        where:
        script                                                                         | serialize | visualize || expectMatchedCst
        "try { println \"test\"; throw \"error\" } catch { println \"error caught\" }" | false     | false     || '47_test'
    }

    def "parses expression statements"() {
        setup:
        def source = new Source("test_script.scr", script)
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        //parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst))

        where:
        script                                               | serialize | visualize || expectMatchedCst
        "println test"                                       | false     | false     || '50_test'
        "local.actor moveto \"anim/my_animation\" \"there\"" | false     | false     || '51_test'
        "local.i = 1"                                        | false     | false     || '52_test'
        "local.i += 1"                                       | false     | false     || '53_test'
        "local.i -= 1"                                       | false     | false     || '54_test'
        "local.array = 1::2::3"                              | false     | false     || '55_test'
        "local.player_name = netname \$player[1]"            | false     | false     || '56_test'
        "local.spawner.group = local.dude.group"             | false     | false     || '57_test'
        "local.case = 1"                                     | false     | false     || '58_test'
        "local.if = 1"                                       | false     | false     || '59_test'
        "local.else = 1"                                     | false     | false     || '60_test'
        "local.while = 1"                                    | false     | false     || '61_test'
        "local.for = 1"                                      | false     | false     || '62_test'
        "local.try = 1"                                      | false     | false     || '63_test'
        "local.catch = 1"                                    | false     | false     || '64_test'
        "local.switch = 1"                                   | false     | false     || '65_test'
        "local.break = 1"                                    | false     | false     || '66_test'
        "local.continue = 1"                                 | false     | false     || '67_test'
        "local.size = 1"                                     | false     | false     || '68_test'
        "local.end = 1"                                      | false     | false     || '69_test'
        "group.climberwaitthread end"                        | false     | false     || '70_test'
        "if (!local.object isTouching \$player) {}"          | false     | false     || '71_test'
    }

    def "parses expressions"() {
        setup:
        def source = new Source("test_script.scr", script)
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst))

        where:
        script                                                   | serialize | visualize || expectMatchedCst
        "local.i = 1"                                            | false     | false     || 'expr_0_test'
        "local.i = -1"                                           | false     | false     || 'expr_1_test'
        "local.i = !1"                                           | false     | false     || 'expr_2_test'
        "local.i = ~1"                                           | false     | false     || 'expr_3_test'
        "local.i = -1 + 2"                                       | false     | false     || 'expr_4_test'
        "local.i = -1 + -2"                                      | false     | false     || 'expr_5_test'
        "local.i = 1 + 2"                                        | false     | false     || 'expr_6_test'
        "local.i = 1 + 2 + 3"                                    | false     | false     || 'expr_7_test'
        "local.i = 1 - 2"                                        | false     | false     || 'expr_8_test'
        "local.i = 1 - 2 - 3"                                    | false     | false     || 'expr_9_test'
        "local.i = 1 * 2"                                        | false     | false     || 'expr_10_test'
        "local.i = 1 * 2 * 3"                                    | false     | false     || 'expr_11_test'
        "local.i = 1 / 2"                                        | false     | false     || 'expr_12_test'
        "local.i = 1 / 2 / 3"                                    | false     | false     || 'expr_13_test'
        "local.i = 1 % 2"                                        | false     | false     || 'expr_14_test'
        "local.i = 1 % 2 % 3"                                    | false     | false     || 'expr_15_test'
        "local.i = 1 == 2"                                       | false     | false     || 'expr_16_test'
        "local.i = 1 == 2 == 3"                                  | false     | false     || 'expr_17_test'
        "local.i = 1 != 2"                                       | false     | false     || 'expr_18_test'
        "local.i = 1 != 2 != 3"                                  | false     | false     || 'expr_19_test'
        "local.i = 1 < 2"                                        | false     | false     || 'expr_20_test'
        "local.i = 1 < 2 < 3"                                    | false     | false     || 'expr_21_test'
        "local.i = 1 > 2"                                        | false     | false     || 'expr_22_test'
        "local.i = 1 > 2 > 3"                                    | false     | false     || 'expr_23_test'
        "local.i = 1 <= 2"                                       | false     | false     || 'expr_24_test'
        "local.i = 1 <= 2 <= 3"                                  | false     | false     || 'expr_25_test'
        "local.i = 1 >= 2"                                       | false     | false     || 'expr_26_test'
        "local.i = 1 >= 2 >= 3"                                  | false     | false     || 'expr_27_test'
        "local.i = 1 & 2"                                        | false     | false     || 'expr_28_test'
        "local.i = 1 & 2 & 3"                                    | false     | false     || 'expr_29_test'
        "local.i = 1 | 2"                                        | false     | false     || 'expr_30_test'
        "local.i = 1 | 2 | 3"                                    | false     | false     || 'expr_31_test'
        "local.i = 1 ^ 2"                                        | false     | false     || 'expr_32_test'
        "local.i = 1 ^ 2 ^ 3"                                    | false     | false     || 'expr_33_test'
        "local.i = 1 + 2 * 3 - 4"                                | false     | false     || 'expr_34_test'
        "local.i = NULL"                                         | false     | false     || 'expr_35_test'
        "local.i = NIL"                                          | false     | false     || 'expr_36_test'
        "local.i = level"                                        | false     | false     || 'expr_37_test'
        "local.i = local.j"                                      | false     | false     || 'expr_38_test'
        "local.i = \"abc\"[0]"                                   | false     | false     || 'expr_39_test'
        "local.i = \"abc\"[0 + 1]"                               | false     | false     || 'expr_40_test'
        "local.i = ( -1 -1 -1 )"                                 | false     | false     || 'expr_41_test'
        "local.i = global/test.scr::main_thread"                 | false     | false     || 'expr_42_test'
        "local.i = exec"                                         | false     | false     || 'expr_43_test'
        "local.i = exec local_thread"                            | false     | false     || 'expr_44_test'
        "local.i = self exec"                                    | false     | false     || 'expr_45_test'
        "local.i = self exec local_thread"                       | false     | false     || 'expr_46_test'
        "local.i = local.j::test"                                | false     | false     || 'expr_47_test'
        "local.i = a & b == c"                                   | false     | false     || 'expr_48_test'
        "local.i = 1 + 2 & 4 * 5"                                | false     | false     || 'expr_49_test'
        "local.i = \$players.size"                               | false     | false     || 'expr_50_test'
        "local.i = -exec local_thread"                           | false     | false     || 'expr_51_test'
        "println abc::~\$players.size[2]"                        | false     | false     || 'expr_52_test'
        "local.x = ( -(1 + 2) \$(1 + 2) -(1))"                   | false     | false     || 'expr_53_test'
        "local.x = ( 1 (a::b) 1)"                                | false     | false     || 'expr_54_test'
        "local.x = (getclientnum \$player[local.i] == local.id)" | false     | false     || 'expr_55_test'
        "local.x = makeArray\n10 20 30\nendArray"                | false     | false     || 'expr_56_test'
        "local.x = makeArray\n10 20 30\na b c\nendArray"         | false     | false     || 'expr_57_test'
    }

    def "parses scripts"() {
        setup:
        def source = new Source(script, loadScr(script, "scripts"))
        def lexer = new Lexer(source)
        def parser = new Parser(source, lexer.scan())

        when:
        def cst = parser.parse()

        then:
        cst != null
        printAnyErrors(parser)
        parser.errors().isEmpty()

        serializeCstToXml(serialize, cst, expectMatchedCst)
        visualizeCst(visualize, cst, expectMatchedCst)
        visualizeAst(visualize, cst, expectMatchedCst)

        assertCstAsExpected(cst, loadXml(expectMatchedCst, 'scripts'))

        where:
        script    | serialize | visualize || expectMatchedCst
        "0_test"  | false     | false     || '0_test'
        "1_test"  | false     | false     || '1_test'
        "2_test"  | false     | false     || '2_test'
        "3_test"  | false     | false     || '3_test'
        "4_test"  | false     | false     || '4_test'
        "5_test"  | false     | false     || '5_test'
        "6_test"  | false     | false     || '6_test'
        "7_test"  | false     | false     || '7_test'
        "8_test"  | false     | false     || '8_test'
        "9_test"  | false     | false     || '9_test'
        "10_test" | false     | false     || '10_test'
        "11_test" | false     | false     || '11_test'
        "12_test" | false     | false     || '12_test'
        "13_test" | false     | false     || '13_test'

    }

    private static Token t(TokenType type) {
        return Token.of(type, "", -1, -1, -1);
    }

    private String loadScr(String scrFilename, String subdir = null) {
        if (subdir == null) {
            return this.class.getResource("/parser/${scrFilename}.scr").text
        } else {
            return this.class.getResource("/parser/$subdir/${scrFilename}.scr").text
        }
    }

    private String loadXml(String xmlFilename, String subdir = null) {
        if (subdir == null) {
            return this.class.getResource("/parser/${xmlFilename}.xml").text
        } else {
            return this.class.getResource("/parser/$subdir/${xmlFilename}.xml").text
        }
    }

    private void printAnyErrors(Parser parser) {
        if (!parser.errors().isEmpty()) {
            parser.errors().each { println it.errorMessage() }
        }
    }

    private void visualizeCst(boolean visualize, com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree cst, String name) {
        if (!visualize) return;

        def graph = new VisualGraphVisitor().visit(cst)
        def file = File.createTempFile("morpheus-lang-$name", ".svg")
        def g = Graphviz.fromGraph(graph)
        g.useEngine(new GraphvizV8Engine())
        g.render(Format.SVG_STANDALONE).toFile(file)
    }

    private void visualizeAst(boolean visualize, com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree cst, String name) {
        if (!visualize) return;

        def ast = new com.github.razorapid.morpheus.lang.cst.visitors.CstToAstVisitor().visit(cst)
        def graph = new VisualAstGraphVisitor().visit(ast)
        def file = File.createTempFile("morpheus-lang-ast-$name", ".svg")
        def g = Graphviz.fromGraph(graph)
        g.useEngine(new GraphvizV8Engine())
        g.render(Format.SVG_STANDALONE).toFile(file)
    }

    private void serializeCstToXml(boolean serialize, com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree cst, String name) {
        if (!serialize) return;

        def xml = new XmlPrinterVisitor(false).visit(cst)
        def file = File.createTempFile("morpheus-lang-$name", ".xml")
        Files.writeString(file.toPath(), xml)
    }

    private void assertCstAsExpected(com.github.razorapid.morpheus.lang.cst.ConcreteSyntaxTree cst, String expectedXml) {
        def xmlTree = new XmlPrinterVisitor(false).visit(cst)
        assertThat(xmlTree).and(expectedXml)
                .ignoreWhitespace()
                .areIdentical()
    }
}
