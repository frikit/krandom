pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // The kRandom snapshot that ./scripts/verify_examples_local.sh publishes; -PkrandomRepository=central
        // resolves the published release from Maven Central instead.
        if (providers.gradleProperty("krandomRepository").orNull != "central") {
            mavenLocal {
                content {
                    includeGroup("io.github.frikit")
                }
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "krandom-kotlin-android-example"

include(":contacts", ":junit-platform")
