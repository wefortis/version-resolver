package libs.common

/**
 * Версия вида `a.b.N`: `a.b` берутся из тега `a.b.x-dev`, `N` — число коммитов после этого тега.
 * Если тега или числа коммитов нет, возвращается [FALLBACK_VERSION].
 */
object VersionCalculator {
    const val FALLBACK_VERSION = "0.0.0-SNAPSHOT"

    private val tagRegex = Regex("""^(\d+)\.(\d+)\.x-dev$""")

    fun calculate(
        tag: String?,
        commitsSinceTag: Int?
    ): String {
        val match = tag?.let(tagRegex::matchEntire)
        if (match == null || commitsSinceTag == null || commitsSinceTag < 0) return FALLBACK_VERSION
        return "${match.groupValues[1]}.${match.groupValues[2]}.$commitsSinceTag"
    }
}
