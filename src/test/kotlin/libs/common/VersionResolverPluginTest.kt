package libs.common

import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VersionResolverPluginTest {
    private fun git(
        dir: File,
        vararg args: String
    ) {
        val process =
            ProcessBuilder("git", "-C", dir.absolutePath, *args)
                .redirectErrorStream(true)
                .start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "git ${args.joinToString(" ")} failed: $output" }
    }

    private fun repository(vararg commitsAfterTag: Int): File {
        val dir = Files.createTempDirectory("version-resolver-test").toFile()
        git(dir, "init", "-q")
        git(dir, "config", "user.email", "test@example.com")
        git(dir, "config", "user.name", "test")
        git(dir, "config", "commit.gpgsign", "false")
        git(dir, "commit", "-q", "--allow-empty", "-m", "initial")
        git(dir, "tag", "1.20.x-dev")
        repeat(commitsAfterTag.firstOrNull() ?: 0) { git(dir, "commit", "-q", "--allow-empty", "-m", "change $it") }
        return dir
    }

    private fun applyPlugin(
        dir: File,
        properties: Map<String, String> = emptyMap()
    ) = ProjectBuilder
        .builder()
        .withProjectDir(dir)
        .build()
        .also { project ->
            properties.forEach { (key, value) -> project.extensions.extraProperties.set(key, value) }
            project.pluginManager.apply("libs.common.version-resolver")
        }

    @Test
    fun `version is major minor and commits after tag`() {
        val project = applyPlugin(repository(3))
        assertEquals("1.20.3", project.version.toString())
        assertNotNull(project.tasks.findByName("printVersion"))
    }

    @Test
    fun `version is applied to subprojects`() {
        val dir = repository(2)
        val root = ProjectBuilder.builder().withProjectDir(dir).build()
        val child = ProjectBuilder.builder().withName("child").withParent(root).build()
        root.pluginManager.apply("libs.common.version-resolver")
        assertEquals("1.20.2", child.version.toString())
    }

    @Test
    fun `property overrides git version`() {
        val project = applyPlugin(repository(3), mapOf("sdkVersion" to "9.9.9"))
        assertEquals("9.9.9", project.version.toString())
    }

    @Test
    fun `falls back outside of a git repository`() {
        val dir = Files.createTempDirectory("version-resolver-no-git").toFile()
        assertEquals(VersionCalculator.FALLBACK_VERSION, applyPlugin(dir).version.toString())
    }
}
