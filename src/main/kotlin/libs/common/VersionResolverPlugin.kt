package libs.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Применяется к корневому проекту: задаёт версию всем проектам и добавляет задачу `printVersion`.
 *
 * Версию можно переопределить свойством `-PsdkVersion=1.2.3`. Для расчёта нужна полная история git с тегами
 * (в CI: `fetch-depth: 0`, `fetch-tags: true`).
 */
class VersionResolverPlugin : Plugin<Project> {
    override fun apply(root: Project) {
        val resolved = resolve(root)
        root.allprojects { version = resolved }
        root.tasks.register("printVersion") {
            group = "help"
            description = "Prints the project version"
            doLast { println(resolved) }
        }
    }

    private fun resolve(root: Project): String {
        root
            .findProperty(VERSION_PROPERTY)
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        val tag = git(root.rootDir, "describe", "--tags", "--match", "*.x-dev", "--abbrev=0")
        val commits = tag?.let { git(root.rootDir, "rev-list", "--count", "$it..HEAD") }?.toIntOrNull()
        return VersionCalculator.calculate(tag, commits)
    }

    private fun git(
        dir: File,
        vararg args: String
    ): String? =
        runCatching {
            val process =
                ProcessBuilder("git", "-C", dir.absolutePath, *args)
                    .redirectErrorStream(true)
                    .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            if (!process.waitFor(GIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                return@runCatching null
            }
            if (process.exitValue() == 0) output else null
        }.getOrNull()

    private companion object {
        const val VERSION_PROPERTY = "sdkVersion"
        const val GIT_TIMEOUT_SECONDS = 30L
    }
}
