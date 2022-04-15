plugins {
  kotlin("jvm") version "1.6.10"
  kotlin("plugin.jpa") version "1.6.10"
  kotlin("plugin.allopen") version "1.6.10"
  kotlin("kapt") version "1.6.20"
  java
  idea
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
  implementation("com.querydsl:querydsl-jpa:5.0.0")
  kapt("com.querydsl:querydsl-apt:5.0.0:jpa")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

idea {
  module {
    val kaptMain = file("build/generated/source/kapt/main")
    sourceDirs.add(kaptMain)
    generatedSourceDirs.add(kaptMain)
  }
}

tasks.getByName<Test>("test") {
  useJUnitPlatform()
}

allOpen {
  annotation("javax.persistence.Entity")
}
