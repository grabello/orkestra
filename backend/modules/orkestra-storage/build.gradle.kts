plugins {
    id("java")
    id("jacoco")
}

dependencies {
    implementation(project(":modules:orkestra-core"))
    implementation(platform("software.amazon.awssdk:bom:2.25.18"))
    implementation("software.amazon.awssdk:dynamodb-enhanced")

    implementation(platform("com.fasterxml.jackson:jackson-bom:2.17.2"))
    implementation ("com.fasterxml.jackson.core:jackson-databind")
    implementation ("com.fasterxml.jackson.core:jackson-core")
    implementation ("com.fasterxml.jackson.core:jackson-annotations")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("org.assertj:assertj-core:3.24.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-core:5.+")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}


// --- Code coverage configuration (JaCoCo) ---

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // always generate report after tests
}

jacoco {
    toolVersion = "0.8.11"
}

val coverageExcludes = listOf(
    "**/generated/**",
    "**/model/**"
)

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)   // useful for CI
        html.required.set(true)  // nice local browsing
        csv.required.set(false)
    }
    // Apply exclusions to the report
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(coverageExcludes)
                }
            }
        )
    )

    doLast {
        val xmlFile = reports.xml.outputLocation.get().asFile
        val htmlDir = reports.html.outputLocation.get().asFile
        println("JaCoCo reports generated:")
        println(" - XML : ${xmlFile.absolutePath}")
        println(" - HTML: ${htmlDir.absolutePath}")
        println("Open HTML report in your browser: file://${htmlDir.resolve("index.html").absolutePath}")
    }

}

tasks.jacocoTestCoverageVerification {
    // Apply the same exclusions to verification
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(coverageExcludes)
                }
            }
        )
    )
    violationRules {
        rule {
            // Overall project coverage rule
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal() // threshold
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.test {
    useJUnitPlatform()
}
