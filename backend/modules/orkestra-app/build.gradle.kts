plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
    id("org.openapi.generator") version "7.6.0"
    id("jacoco")
}

dependencies {
    // For @Schema etc (Boot 3 / jakarta variant)
    compileOnly("io.swagger.core.v3:swagger-annotations-jakarta:2.2.22")

    implementation(project(":modules:orkestra-api"))
    implementation(project(":modules:orkestra-core"))
    implementation(project(":modules:orkestra-storage"))
    implementation(project(":modules:orkestra-dsl"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation(platform("software.amazon.awssdk:bom:2.25.18"))
    implementation("software.amazon.awssdk:dynamodb")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${rootDir}/../docs/api/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)

    // Generate ONLY APIs (interfaces), no models/supporting files
    globalProperties.set(
        mapOf(
            "apis" to "",
            "models" to "false",
            "supportingFiles" to "false",
            "apiTests" to "false",
            "apiDocs" to "false"
        )
    )

    apiPackage.set("com.orkestra.app.web.generated")
    modelPackage.set("com.orkestra.api.model")

    configOptions.set(
        mapOf(
            "useSpringBoot3" to "true",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "useTags" to "true",
            "openApiNullable" to "false",
            "useJakartaEe" to "true"
        )
    )
}

// Add generated sources to compilation
sourceSets {
    named("main") {
        java.srcDir(layout.buildDirectory.dir("generated/src/main/java"))
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.named("openApiGenerate"))
}

// --- Code coverage configuration (JaCoCo) ---

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // generate report after tests
}

jacoco {
    toolVersion = "0.8.11"
}

val coverageExcludes = listOf(
    "**/CorrelationIdFilter.class",
    "**/OrkestraApplication.class",
    "**/config/**",
    "**/generated/**"
)

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) { exclude(coverageExcludes) }
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
                fileTree(it) { exclude(coverageExcludes) }
            }
        )
    )
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal() // fail build under 80%
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
