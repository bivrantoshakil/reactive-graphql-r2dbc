import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.2.0"
    id("org.jmailen.kotlinter") version "3.13.0"
}

group = "jp.cp.anyx"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    // for custom scalar support in graphql
    implementation("com.tailrocks.graphql:graphql-datetime-kickstart-spring-boot-starter:6.0.0")
    implementation("org.flywaydb:flyway-core")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.amshove.kluent:kluent:1.68")
    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("org.testcontainers:postgresql:1.17.6")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType()

tasks.withType<Test> {
    useJUnitPlatform()
}

// install ktlint hook for pre - push code linting
tasks.check {
    dependsOn("installKotlinterPrePushHook")
}
