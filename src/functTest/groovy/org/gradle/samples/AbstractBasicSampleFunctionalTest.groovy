package org.gradle.samples


import org.gradle.guides.TestFile
import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

abstract class AbstractBasicSampleFunctionalTest extends AbstractSampleFunctionalSpec {
    def "can build samples"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":generateWrapperForSamples").outcome == SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result)
        and:
        def indexFile = file("build/samples/docs/index.adoc")
        indexFile.text.contains('- <<sample_demo#,Demo>>')
        assertReadmeHasContent()
        and:
        assertDslZipsHaveContent()
    }

    def "can assemble sample using a lifecycle task"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipFilesExists()
    }

    def "defaults to Gradle version based on the running distribution"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        usingGradleVersion("6.0")
        build("assembleDemoSample")

        then:
        dslZipFiles.each {
            assertGradleWrapperVersion(it, '6.0')
        }

        when:
        usingGradleVersion('6.0.1')
        build("assembleDemoSample")

        then:
        dslZipFiles.each {
            assertGradleWrapperVersion(it, '6.0.1')
        }
    }

    def "can relocate sample"() {
        makeSingleProject()
        writeSampleUnderTest('src')
        buildFile << """
            ${sampleUnderTestDsl} {
                sampleDirectory = file('src')
            }
        """

        when:
        build("assembleDemoSample")

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()
    }

    def "defaults sample location to `src/samples/<sample-name>`"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert ${sampleUnderTestDsl}.sampleDirectory.get().asFile.absolutePath == '${file('src/samples/demo').canonicalPath}'
                }
            }
        """

        when:
        build("verify")

        then:
        noExceptionThrown()
    }

    def "can access the readme file location from the sample"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert ${sampleUnderTestDsl}.readMeFile.get().asFile.canonicalPath == '${new File(temporaryFolder.root, "src/samples/demo/README.adoc").canonicalPath}'
                }
            }
        """

        expect:
        build('verify')
        result.task(':verify').outcome == SUCCESS
    }

    @Unroll
    def "adds license to archive when found within the root directory"() {
        makeSingleProject()
        writeSampleUnderTest()
        file("LICENSE") << "Some license"

        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()
    }

    @Unroll
    def "excludes '#directory' when building the domain language archive"() {
        makeSingleProject()
        writeSampleUnderTest()
        file("src/samples/demo/common/${directory}/foo.txt") << "Exclude"
        buildFile << """
def sample = ${sampleUnderTestDsl}
sample.common {
    from(sample.sampleDirectory.file("common"))
}
"""
        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()

        where:
        directory << ['.gradle', 'build']
    }

    protected abstract List<TestFile> getDslZipFiles()

    protected abstract void assertSampleTasksExecutedAndNotSkipped(BuildResult result)

    protected abstract void assertReadmeHasContent()

    protected abstract void assertDslZipsHaveContent()

    protected abstract void assertDslZipFilesExists()
}
