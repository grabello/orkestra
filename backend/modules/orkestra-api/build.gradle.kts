plugins {
    id("java-library")
    id("org.openapi.generator") version "7.6.0"
}

dependencies {
    // For @Schema etc (Boot 3 / jakarta variant)
    compileOnly("io.swagger.core.v3:swagger-annotations-jakarta:2.2.22")
    // Needed for @jakarta.annotation.Generated / @Nullable if emitted
    compileOnly("jakarta.annotation:jakarta.annotation-api:2.1.1")
    // If the generated models include validation annotations
    compileOnly("jakarta.validation:jakarta.validation-api:3.0.2")

    api("com.fasterxml.jackson.core:jackson-annotations:2.17.+" )


    // Needed for org.springframework.format.annotation.DateTimeFormat
    compileOnly("org.springframework:spring-context:6.2.16")

    compileOnly("jakarta.annotation:jakarta.annotation-api:2.1.1")
    compileOnly("io.swagger.core.v3:swagger-annotations-jakarta:2.2.22")

    compileOnly("jakarta.validation:jakarta.validation-api:3.0.2")
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${rootDir}/../docs/api/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)

    globalProperties.set(
        mapOf(
            "models" to "",        // generate models
            "apis" to "false",     // no controllers/interfaces here
            "supportingFiles" to "false",
            "modelTests" to "false",
            "modelDocs" to "false"
        )
    )

    modelPackage.set("com.orkestra.api.model")

    configOptions.set(
        mapOf(
            "useSpringBoot3" to "true",
            "useJakartaEe" to "true",
            "openApiNullable" to "false",
            "dateLibrary" to "java8",
            )
    )
}

sourceSets {
    named("main") { java.srcDir(layout.buildDirectory.dir("generated/src/main/java")) }
}

tasks.named("compileJava") { dependsOn(tasks.named("openApiGenerate")) }
