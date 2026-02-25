plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
    id("org.openapi.generator") version "7.6.0"
}

dependencies {
    // For @Schema etc (Boot 3 / jakarta variant)
    compileOnly("io.swagger.core.v3:swagger-annotations-jakarta:2.2.22")

    implementation(project(":modules:orkestra-api"))
    implementation(project(":modules:orkestra-core"))
    implementation(project(":modules:orkestra-storage"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation(platform("software.amazon.awssdk:bom:2.25.18"))
    implementation("software.amazon.awssdk:dynamodb")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
