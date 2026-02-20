plugins {
    `java`
    `maven-publish`
    signing
    application
}

group = "io.github.sinio-manoka"
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

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/Sinio-Manoka/quill/issues")
                }
            }
        }
    }

    repositories {
        // GitHub Packages (automatic publishing)
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

// Signing configuration (for Maven Central bundle)
signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Sign>().configureEach {
    // Only sign during release, not during regular builds
    onlyIf { project.hasProperty("release") }
}

// Task to generate checksums for Maven Central bundle
tasks.register("generateChecksums") {
    dependsOn("build", "publishToMavenLocal")

    val libsDir = layout.buildDirectory.dir("libs").get().asFile
    val mavenDir = file("${System.getProperty("user.home")}/.m2/repository/io/github/sinio-manoka/quill/${project.version}")

    inputs.dir(libsDir)
    inputs.dir(mavenDir)

    doLast {
        // Generate checksums for JAR files
        val jarFiles = listOf(
            libsDir.resolve("quill-${project.version}.jar"),
            libsDir.resolve("quill-${project.version}-javadoc.jar"),
            libsDir.resolve("quill-${project.version}-sources.jar")
        )

        // Generate checksums for POM and module files
        val pomFiles = listOf(
            mavenDir.resolve("quill-${project.version}.pom"),
            mavenDir.resolve("quill-${project.version}.module")
        )

        (jarFiles + pomFiles).forEach { file ->
            if (file.exists()) {
                // Generate MD5 (creates .MD5 file)
                ant.invokeMethod("checksum", mapOf(
                    "file" to file,
                    "algorithm" to "MD5",
                    "todir" to file.parentFile
                ))
                // Generate SHA-1 (creates .SHA-1 file)
                ant.invokeMethod("checksum", mapOf(
                    "file" to file,
                    "algorithm" to "SHA-1",
                    "todir" to file.parentFile
                ))
                // Rename uppercase extensions to lowercase
                file.resolveSibling("${file.name}.MD5")
                    .takeIf { it.exists() }?.renameTo(file.resolveSibling("${file.name}.md5"))
                file.resolveSibling("${file.name}.SHA-1")
                    .takeIf { it.exists() }?.renameTo(file.resolveSibling("${file.name}.sha1"))
            }
        }
    }
}

// Task to create a Maven Central bundle for manual upload
tasks.register<Zip>("mavenCentralBundle") {
    archiveFileName.set("quill-${project.version}-bundle.zip")
    destinationDirectory.set(layout.buildDirectory.dir("bundle"))

    dependsOn("build", "publishToMavenLocal", "generateChecksums")

    // JAR files with signatures and checksums from build/libs
    from(layout.buildDirectory.dir("libs")) {
        include("quill-${project.version}.jar")
        include("quill-${project.version}.jar.asc")
        include("quill-${project.version}.jar.md5")
        include("quill-${project.version}.jar.sha1")
        include("quill-${project.version}-javadoc.jar")
        include("quill-${project.version}-javadoc.jar.asc")
        include("quill-${project.version}-javadoc.jar.md5")
        include("quill-${project.version}-javadoc.jar.sha1")
        include("quill-${project.version}-sources.jar")
        include("quill-${project.version}-sources.jar.asc")
        include("quill-${project.version}-sources.jar.md5")
        include("quill-${project.version}-sources.jar.sha1")
    }

    // POM, module files with signatures and checksums from Maven local
    from("${System.getProperty("user.home")}/.m2/repository/io/github/sinio-manoka/quill/${project.version}") {
        include("quill-${project.version}.pom")
        include("quill-${project.version}.pom.asc")
        include("quill-${project.version}.pom.md5")
        include("quill-${project.version}.pom.sha1")
        include("quill-${project.version}.module")
        include("quill-${project.version}.module.asc")
        include("quill-${project.version}.module.md5")
        include("quill-${project.version}.module.sha1")
    }

    into("io/github/sinio-manoka/quill/${project.version}")
}
