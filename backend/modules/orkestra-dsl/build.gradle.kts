plugins {
    id("java")
    id("jacoco")
}

dependencies {
    implementation(project(":modules:orkestra-core"))

    implementation("org.yaml:snakeyaml:2.2")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("org.assertj:assertj-core:3.24.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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
    "**/generated/**"
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
