package org.gradle.docs.samples

import org.gradle.docs.Dsl

class TestWithExemplarExplicitBothDslSampleFunctionalTest extends AbstractExemplarBothDslSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        super.makeSingleProject()
        buildFile << """
            import ${Dsl.canonicalName}
            documentation.samples.publishedSamples.all {
                dsls = [ Dsl.KOTLIN, Dsl.GROOVY ]
            }
        """
    }
}
