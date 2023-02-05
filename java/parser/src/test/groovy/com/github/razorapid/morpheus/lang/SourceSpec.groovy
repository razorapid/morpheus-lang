package com.github.razorapid.morpheus.lang

import spock.lang.Specification

class SourceSpec extends Specification {

    def "gives proper source line by it's line number"() {
        given:
        def source = new Source(script, loadScript(script))

        expect:
        source.line(lineNo).get() == expectedLine

        where:

        script       | lineNo || expectedLine
        '1_test.scr' | 1      || 'conprintf "HERE I AM\\n";'
        '1_test.scr' | 2      || 'println "TEST TEST";'
        '1_test.scr' | 13     || '\tlevel.players = local CreateListener'
        '1_test.scr' | 72     || 'end local.ipOnly'
    }

    def "no line present for line number outside of source range"() {
        given:
        def source = new Source(script, loadScript(script))

        expect:
        source.line(lineNo).isEmpty()

        where:

        script       | lineNo || _
        '6_test.scr' | 72     || _
    }

    private String loadScript(String scriptFilename) {
        return this.class.getResource("/lexer/" + scriptFilename).text
    }
}
