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

    // Competitor libraries for comparative benchmarks
    implementation("net.datafaker:datafaker:2.6.0")
    implementation("org.jeasy:easy-random-core:5.0.0")
    implementation("org.instancio:instancio-core:5.6.0")
    implementation("com.github.javafaker:javafaker:1.0.2") {
        exclude(group = "org.yaml", module = "snakeyaml")
    }
    implementation("org.yaml:snakeyaml:2.6")
}

tasks.register<JavaExec>("jmh") {
    group = "benchmark"
    description = "Run JMH microbenchmarks."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.openjdk.jmh.Main")
    if (project.hasProperty("jmhArgs")) {
        args(project.property("jmhArgs").toString().split(" "))
    } else {
        args("io.github.frikit.krandom.benchmarks.*")
    }
}

tasks.register<JavaExec>("profileGeneration") {
    group = "benchmark"
    description = "Run macro profiling for large-scale generation workloads."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.frikit.krandom.benchmarks.GenerationProfileRunner")
}
