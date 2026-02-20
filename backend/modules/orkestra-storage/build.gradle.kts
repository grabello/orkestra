plugins {
    id("java")
}

dependencies {
    implementation(project(":modules:orkestra-core"))
    implementation(platform("software.amazon.awssdk:bom:2.25.18"))
    implementation("software.amazon.awssdk:dynamodb")
}
