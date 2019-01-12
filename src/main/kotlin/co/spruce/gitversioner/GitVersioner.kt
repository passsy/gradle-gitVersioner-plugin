@file:Suppress("RedundantVisibilityModifier")

package co.spruce.gitversioner

import org.gradle.api.logging.Logger
import java.util.concurrent.TimeUnit

private val YEAR_IN_SECONDS = TimeUnit.DAYS.toSeconds(365)

public val NO_CHANGES = LocalChanges(0, 0, 0)

public open class GitVersioner internal constructor(
        private val gitInfoExtractor: GitInfoExtractor,
        private val logger: Logger? = null) {

    public var baseBranch: String = "master"
    public var yearFactor: Int = 1000

    public var formatter: ((GitVersioner) -> CharSequence) = DEFAULT_FORMATTER

    public var ciBranchNameProvider: () -> CharSequence? = {
        // get branch name on jenkins
        System.getenv("BRANCH") ?: System.getenv("BRANCH_NAME")
    }

    /**
     * base branch commit count + [timeComponent]
     */
    public val versionCode: Int by lazy {
        when {
            gitInfoExtractor.isGitProjectReady -> baseBranchCommits.size + timeComponent
            else -> -1
        }.also { logger?.debug("git versionCode: $it") }
    }

    /**
     * string representation powered by [formatter]
     */
    public val versionName: String by lazy {
        var name = "undefined"
        if (gitInfoExtractor.isGitProjectReady) {
            name = try {
                formatter(this).toString()
            } catch (e: Throwable) {
                logger?.info("formatter failed to generate a correct name, using default formatter")
                DEFAULT_FORMATTER(this).toString()
            }
            logger?.debug("git versionName: $name")
        }
        return@lazy name
    }

    /**
     * the current local changes (files changed, additions, deletions). [NO_CHANGES] when no changes detected
     */
    public val localChanges: LocalChanges by lazy {
        if (!gitInfoExtractor.isGitProjectReady) NO_CHANGES else gitInfoExtractor.localChanges
    }

    /**
     * the name of the branch HEAD is currently on
     */
    public val branchName: String?
        get() {
            if (!gitInfoExtractor.isGitProjectReady) return null
            return gitInfoExtractor.currentBranch ?: ciBranchNameProvider()?.toString()
        }

    /**
     * all commits in [baseBranch] without the [featureBranchCommits]
     */
    public val baseBranchCommitCount: Int by lazy { baseBranchCommits.count() }

    /**
     * commits on feature branch not in [baseBranch]
     */
    public val featureBranchCommitCount: Int by lazy { featureBranchCommits.count() }

    /**
     * full sha1 of current commit
     *
     * @see [currentSha1Short]
     */
    public val currentSha1: String? by lazy { gitInfoExtractor.currentSha1 }

    /**
     * 7 char sha1 of current commit
     */
    public val currentSha1Short: String? by lazy { gitInfoExtractor.currentSha1?.take(7) }

    /**
     * [yearFactor] based time component from initial commit to [featureBranchOriginCommit]
     */
    public val timeComponent: Int by lazy {
        if (!gitInfoExtractor.isGitProjectReady) return@lazy 0
        val latestBaseCommit = featureBranchOriginCommit ?: return@lazy 0

        val timeToHead = gitInfoExtractor.commitDate(
                latestBaseCommit) - gitInfoExtractor.initialCommitDate
        return@lazy (timeToHead * yearFactor / YEAR_IN_SECONDS + 0.5).toInt()
    }

    /**
     * last commit in base branch which is parent of HEAD, most likely where the
     * feature branch was created or the last base branch commit which was merged
     * into the feature branch
     */
    public val featureBranchOriginCommit: String? by lazy { baseBranchCommits.firstOrNull() }

    /**
     * sha1 of the initial commit of the git tree, first commit
     */
    public val initialCommit: String? by lazy { gitInfoExtractor.commitsToHead.lastOrNull() }

    /**
     * whether git can be used to extract data
     */
    public val isGitInitialized: Boolean = gitInfoExtractor.isGitProjectReady

    /**
     * commits of base branch in history of current commit (HEAD).
     */
    private val baseBranchCommits: List<String> by lazy {
        val baseCommits = gitInfoExtractor.commitsUpTo(baseBranch)
        baseCommits.forEach { baseCommit ->
            if (gitInfoExtractor.commitsToHead.contains(baseCommit)) {
                return@lazy baseCommits
            }
        }

        return@lazy emptyList<String>()
    }

    /**
     * commits on the feature branch not merged into [baseBranch]
     */
    private val featureBranchCommits: List<String> by lazy {
        gitInfoExtractor.commitsToHead.filter { !baseBranchCommits.contains(it) }
    }

    companion object {

        @JvmStatic
        public val DEFAULT_FORMATTER: ((GitVersioner) -> CharSequence) = { versioner ->
            with(versioner) {
                val sb = StringBuilder(versioner.versionCode.toString())
                val hasCommits = featureBranchCommitCount > 0 || baseBranchCommitCount > 0
                val isFeatureBranch = baseBranch != branchName
                if (isFeatureBranch && hasCommits)
                    sb.append("-${branchName ?: "undefined"}")
                if ((localChanges != NO_CHANGES) || isFeatureBranch)
                    sb.append("-SNAPSHOT-${System.currentTimeMillis() / 1000}")

                sb.toString().replace("[^a-zA-Z0-9-]".toRegex(), "-")
            }
        }

    }
}

public data class LocalChanges(
        val filesChanged: Int = 0,
        val additions: Int = 0,
        val deletions: Int = 0) {

    override fun toString(): String = "$filesChanged +$additions -$deletions"

    fun shortStats(): String =
            if (filesChanged + additions + deletions == 0) "no changes"
            else "files changed: $filesChanged, additions(+): $additions, deletions(-): $deletions"
}