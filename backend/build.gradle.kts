plugins {
	// 버전은 루트에서 apply false로 관리 중이므로 여기서는 명시하지 않음
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	java
}

group = "org.example"
// version은 루트 기본 사용 가능

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

dependencies {
	implementation(project(":shared:common"))

	// Spring starters (버전은 BOM이 관리)
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-batch")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// ByteBuddy (BOM이 관리)
	implementation("net.bytebuddy:byte-buddy")
	implementation("net.bytebuddy:byte-buddy-agent")

	// DB
	implementation("com.h2database:h2")
	implementation("org.postgresql:postgresql") // 버전 제거 → BOM이 관리

	// Kafka
	implementation("org.springframework.kafka:spring-kafka")

	// Actuator + Prometheus
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	// Security + OAuth2
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

	// Swagger (BOM 미관리 → 버전 유지)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")

	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.apache.commons:commons-pool2")

	// JWT (BOM 미관리 → 버전 유지)
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// jsoup (BOM 미관리 → 버전 유지)
	implementation("org.jsoup:jsoup:1.21.1")

	// QueryDSL (필요 모듈에서만 유지)
	implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
	annotationProcessor("jakarta.annotation:jakarta.annotation-api")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")

	// 테스트 (기본은 루트 공통 제공)
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.springframework.batch:spring-batch-test")
	// testRuntimeOnly("org.junit.platform:junit-platform-launcher") // 루트 공통
}

tasks.withType<Test> {
	useJUnitPlatform()
}
