plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "libs.common"
version = providers.gradleProperty("pluginVersion").getOrElse("1.0.0")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("versionResolver") {
            id = "libs.common.version-resolver"
            implementationClass = "libs.common.VersionResolverPlugin"
            displayName = "Version resolver"
            description = "Sets the project version to <major>.<minor>.<commits since tag> from the nearest *.x-dev git tag"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        // Публикация в GitHub Packages: нужны GITHUB_ACTOR и GITHUB_TOKEN (scope write:packages)
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/wefortis/version-resolver")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
