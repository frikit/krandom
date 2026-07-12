plugins {
    base
}

tasks.register("verifyJpms") {
    group = "verification"
    description = "Runs the open and closed named-module object-generation contracts."
    dependsOn(":closed-consumer:run", ":open-consumer:run", ":jackson-consumer:run", ":junit-consumer:run")
}
