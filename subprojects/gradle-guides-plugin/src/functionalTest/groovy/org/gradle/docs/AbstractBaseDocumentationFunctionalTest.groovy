package org.gradle.docs

import spock.lang.Unroll

abstract class AbstractBaseDocumentationFunctionalTest extends AbstractFunctionalTest {
    @Unroll
    def "fails if disallowed characters in documentation element name (#name)"(String name) {
        buildFile << applyDocumentationPlugin() << createDocumentationElement(name)

        expect:
        def result = buildAndFail('help')
        result.output.contains("'${name}' has disallowed characters")

        where:
        name << ['foo_bar', 'foo-bar']
    }

    protected static String applyDocumentationPlugin() {
        return  """
            plugins {
                id 'org.gradle.documentation'
            }
        """
    }

    protected abstract String createDocumentationElement(String name)
}
