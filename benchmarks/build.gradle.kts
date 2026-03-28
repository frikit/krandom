plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":core"))
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.register<JavaExec>("jmh") {
    group = "benchmark"
    description = "Run JMH microbenchmarks."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.openjdk.jmh.Main")
    args("org.github.krandom.benchmarks.*")
}

tasks.register<JavaExec>("profileGeneration") {
    group = "benchmark"
    description = "Run macro profiling for large-scale generation workloads."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.github.krandom.benchmarks.GenerationProfileRunner")
}
