plugins {
    // 공통 플러그인 버전은 여기에서만 관리
    java

    id("org.springframework.boot") version "3.5.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

// 공통 group/version (모듈에서 덮어써도 됨)
allprojects {
    group = "org.example"
    version = "0.0.1-SNAPSHOT"
}

// 전 모듈 공통 설정
subprojects {
    // 모든 자바 모듈에 java 플러그인 적용
    apply(plugin = "java")
    // Spring의 의존성 BOM 및 관리 플러그인 공통 적용
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    // annotationProcessor를 compileOnly로도 확장 (롬복 등)
    configurations {
        val annotationProcessor by getting
        named("compileOnly") {
            extendsFrom(annotationProcessor)
        }
        named("testCompileOnly") {
            extendsFrom(annotationProcessor)
        }
    }

    // 공통 BOM (각 모듈에서 버전 명시 없이 starters 사용 가능)
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.0")
        }
    }

    dependencies {
        // Lombok (전 모듈 공통)
        "compileOnly"("org.projectlombok:lombok")
        "annotationProcessor"("org.projectlombok:lombok")
        "testCompileOnly"("org.projectlombok:lombok")
        "testAnnotationProcessor"("org.projectlombok:lombok")

        // 공통 테스트 의존성
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
