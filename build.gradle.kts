plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.jpa") version "1.6.10"
    java
}

group = "net.joostory"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.h2database:h2:2.1.210")
    implementation("org.hibernate:hibernate-entitymanager:5.6.7.Final")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
