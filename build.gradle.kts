plugins {
    `java`
    `maven-publish`
    application
}

group = "com.github.Sinio-Manoka"
version = "1.0.0"

repositories {
    mavenCentral()
}

application {
    mainClass = "com.quill.Main"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.release.set(21)
    options.compilerArgs.add("--enable-preview")
}

tasks.test {
    jvmArgs("--enable-preview")
    useJUnitPlatform()
}

tasks.run {
    jvmArgs("--enable-preview")
}

tasks.javadoc {
    options.encoding = "UTF-8"
    if (options is StandardJavadocDocletOptions) {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        (options as StandardJavadocDocletOptions).addStringOption("-source", "21")
        (options as StandardJavadocDocletOptions).addBooleanOption("-enable-preview", true)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "quill"
            from(components["java"])

            pom {
                name.set("Quill")
                description.set("A modern, structured logging library for Java 21+ with JSON-first output, zero dependencies, and virtual thread support.")
                url.set("https://github.com/Sinio-Manoka/quill")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("sinio-manoka")
                        name.set("Sinio Manoka")
                        url.set("https://github.com/Sinio-Manoka")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Sinio-Manoka/quill.git")
                    developerConnection.set("scm:git:ssh://github.com/Sinio-Manoka/quill.git")
                    url.set("https://github.com/Sinio-Manoka/quill")
                }

                // Maven Central requires signing info
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/Sinio-Manoka/quill/issues")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Sinio-Manoka/quill")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}