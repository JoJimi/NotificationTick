plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.example.shared"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.0")
    }
}

dependencies {
    // 공통 최소 의존성
    api("org.springframework.boot:spring-boot-starter-validation")

    // JPA 어노테이션
    compileOnly("jakarta.persistence:jakarta.persistence-api")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Querydsl
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    compileOnly("org.springframework:spring-web")
    compileOnly("org.springframework.security:spring-security-core")
    compileOnly("org.springframework.security:spring-security-oauth2-jose")

    testCompileOnly("org.springframework:spring-web")
    testCompileOnly("org.springframework.security:spring-security-core")
    testCompileOnly("org.springframework:spring-security-oauth2-jose")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}
