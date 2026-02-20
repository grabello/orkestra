plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
}

dependencies {
    implementation(project(":modules:orkestra-core"))
    implementation(project(":modules:orkestra-engine"))
    implementation(project(":modules:orkestra-dsl"))
    implementation(project(":modules:orkestra-api"))
    implementation(project(":modules:orkestra-storage"))

    implementation(platform("software.amazon.awssdk:bom:2.25.18"))

    implementation("software.amazon.awssdk:dynamodb")
    implementation("software.amazon.awssdk:auth")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
