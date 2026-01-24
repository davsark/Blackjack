plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

dependencies {
    // Proyecto común (protocolo compartido)
    implementation(project(":composeApp"))

    // Corrutinas
    implementation(libs.kotlinx.coroutinesCore)

    // Serialización JSON
    implementation(libs.kotlinx.serializationJson)

    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}

application {
    mainClass.set("org.example.project.server.GameServerKt")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
