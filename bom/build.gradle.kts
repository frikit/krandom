plugins {
    `java-platform`
}

dependencies {
    constraints {
        api(project(":core"))
        api(project(":jackson"))
        api(project(":junit"))
        api(project(":spring-boot-starter"))
        api(project(":kotest-extensions"))
        api(project(":kotlin-dsl"))
    }
}
