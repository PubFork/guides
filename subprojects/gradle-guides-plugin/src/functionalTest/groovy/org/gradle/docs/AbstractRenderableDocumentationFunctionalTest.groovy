package org.gradle.docs

import spock.lang.Unroll

abstract class AbstractRenderableDocumentationFunctionalTest extends AbstractFunctionalTest {
    def "defaults documentation element display name to title case"() {
        buildFile << applyDocumentationPlugin() << createDocumentationElement('foo') << createDocumentationElement('fooBar')
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert ${documentationDsl('foo')}.displayName.get() == 'Foo'
                    assert ${documentationDsl('fooBar')}.displayName.get() == 'Foo Bar'
                }
            }
        """

        expect:
        build('verify')
    }

    def "detaults documentation element description to empty string"() {
        buildFile << applyDocumentationPlugin() << createDocumentationElement('foo') << createDocumentationElement('fooBar')
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert ${documentationDsl('foo')}.description.get() == ''
                    assert ${documentationDsl('fooBar')}.description.get() == ''
                }
            }
        """

        expect:
        build('verify')
    }

    def "can detect dead links"() {
        given:
        makeSingleProject()
        writeDocumentationUnderTest()
        contentFileUnderTest << '''
            |https://not.existant/url
            |'''.stripMargin()

        expect:
        def result = buildAndFail(checkTaskNameUnderTest)
        result.output.contains('''> The following links are broken:
            |   https://not.existant/url'''.stripMargin())
    }

    def "fails rendering on error due to missing includes"() {
        makeSingleProject()
        writeDocumentationUnderTest()
        contentFileUnderTest << """
            |include::step-1.adoc[]
            |
            |include::step-2.adoc[]
            |""".stripMargin()

        expect:
        buildAndFail('assemble')
    }

    def "fails rendering on error due to no callout found"() {
        makeSingleProject()
        writeDocumentationUnderTest()
        contentFileUnderTest << """
            |[listing]
            |----
            |Some listing without callout
            |----
            |<1> Generated folder for wrapper files
            |""".stripMargin()

        expect:
        buildAndFail('assemble')
    }

    protected static String applyDocumentationPlugin() {
        return  """
            plugins {
                id 'org.gradle.documentation'
            }
        """
    }

    protected abstract String getCheckTaskNameUnderTest()

    protected abstract String createDocumentationElement(String name)

    protected abstract String documentationDsl(String name)

    protected abstract void makeSingleProject()

    protected abstract void writeDocumentationUnderTest()

    protected abstract TestFile getContentFileUnderTest()
}
