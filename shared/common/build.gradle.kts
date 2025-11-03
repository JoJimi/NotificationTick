plugins {
    `java-library`
    // dependency-management는 루트에서 공통 적용했으므로 제거
}

group = "org.example.shared"
// version은 루트 기본값을 사용하거나 필요 시 유지 가능
// version = "0.0.1-SNAPSHOT"

dependencies {
    // 공통 최소 의존성 (BOM 덕분에 버전 명시 불필요)
    api("org.springframework.boot:spring-boot-starter-validation")

    // JPA/스프링 어노테이션을 "참조만" 필요할 때
    compileOnly("jakarta.persistence:jakarta.persistence-api")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")

    // Querydsl (공유 모듈에서 엔티티/쿼리 타입 노출 필요 시 유지)
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // 웹/시큐리티 API 의존(컴파일타임 참조만)
    compileOnly("org.springframework:spring-web")
    compileOnly("org.springframework.security:spring-security-core")
    compileOnly("org.springframework.security:spring-security-oauth2-jose")

    testCompileOnly("org.springframework:spring-web")
    testCompileOnly("org.springframework.security:spring-security-core")
    testCompileOnly("org.springframework.security:spring-security-oauth2-jose")
    // testImplementation/런처는 루트에서 공통 제공됨
}
