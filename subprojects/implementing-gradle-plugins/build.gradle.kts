import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/implementing-gradle-plugins")
    minimumGradleVersion.set("5.0")
    category.set("Topical")
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
}

dependencies {
    docsTestImplementation("org.gradle:sample-check:0.12.6")
    docsTestImplementation(gradleTestKit())
}

// TODO: doesn't seems to have any command to run
tasks.named("checkAsciidoctorGuideContents") {
    enabled = false
}
