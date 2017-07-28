@file:Suppress("RedundantVisibilityModifier")

package com.pascalwelsch.gitversioner

import org.gradle.api.logging.Logger
import java.util.concurrent.TimeUnit

private val YEAR_IN_SECONDS = TimeUnit.DAYS.toSeconds(365)

public val NO_CHANGES = LocalChanges(0, 0, 0)

public open class GitVersioner internal constructor(
    private val gitInfoExtractor: GitInfoExtractor,
    private val logger: Logger? = null
) {

    public var baseBranch: String = "master"

    public var yearFactor: Int = 1000

    public var addSnapshot: Boolean = true

    public var addLocalChangesDetails: Boolean = true

    public var formatter: ((GitVersioner) -> CharSequence) = DEFAULT_FORMATTER

    public var shortNameFormatter: ((GitVersioner) -> CharSequence) = DEFAULT_SHORT_NAME_FORMATTER

    public var ciBranchNameProvider: () -> CharSequence? = {
        // get branch name on jenkins
        System.getenv("BRANCH") ?: System.getenv("BRANCH_NAME") ?: System.getenv("GITREF")
    }

    //TODO add offset
    /**
     * base branch commit count + [timeComponent]
     */
    @Deprecated("converted to property", replaceWith = ReplaceWith("versionCode"))
    public fun versionCode(): Int {
        logger?.warn("The GitVersioner.versionCode() method has been deprecated, " +
                "use the property GitVersioner.versionCode instead")
        return versionCode
    }

    /**
     * base branch commit count + [timeComponent]
     */
    public val versionCode: Int by lazy {
        try {
            requireWorkingGit()
            requireWorkingHistory()
            val commitComponent = baseBranchCommits.size
            val code = commitComponent + timeComponent
            logger?.debug("git versionCode: $code")
            return@lazy code
        } catch (e: Throwable) {
            // fallback
            // must be a positive number for android projects
            return@lazy 1
        }
    }

    /**
     * string representation powered by [formatter]
     */
    @Deprecated("converted to property", replaceWith = ReplaceWith("versionName"))
    public fun versionName(): String {
        logger?.warn("The GitVersioner.versionName() method has been deprecated, " +
                "use the property GitVersioner.versionName instead")
        return versionName
    }

    /**
     * string representation powered by [formatter]
     */
    public val versionName: String by lazy {
        try {
            requireWorkingGit()
            val name = try {
                formatter(this).toString()
            } catch (e: Throwable) {
                logger?.info("formatter failed to generate a correct name, using default formatter")
                DEFAULT_FORMATTER(this).toString()
            }
            logger?.debug("git versionName: $name")
            return@lazy name
        } catch (e: Throwable) {
            // fallback
            // Can't be null for android projects
            return@lazy "undefined"
        }
    }

    /**
     * the current local changes (files changed, additions, deletions). [NO_CHANGES] when no changes detected
     */
    public val localChanges: LocalChanges by lazy {
        try {
            requireWorkingGit()
            return@lazy gitInfoExtractor.localChanges
        } catch (e: Throwable) {
            return@lazy NO_CHANGES
        }
    }

    /**
     * the name of the branch HEAD is currently on
     */
    public val branchName: String?
        get() {
            return try {
                gitInfoExtractor.currentBranch ?: ciBranchNameProvider()?.toString()
            } catch (e: Throwable) {
                null
            }
        }

    /**
     * all commits in [baseBranch] without the [featureBranchCommits]
     */
    public val baseBranchCommitCount: Int by lazy {
        try {
            requireWorkingGit()
            requireWorkingHistory()
            return@lazy baseBranchCommits.count()
        } catch (e: Throwable) {
            return@lazy 0
        }
    }

    /**
     * commits on feature branch not in [baseBranch]
     */
    public val featureBranchCommitCount: Int by lazy {
        try {
            requireWorkingGit()
            requireWorkingHistory()
            return@lazy featureBranchCommits.count()
        } catch (e: Throwable) {
            return@lazy 0
        }
    }

    /**
     * all commits together, from initial commit to HEAD
     */
    public val commitCount: Int by lazy { baseBranchCommitCount + featureBranchCommitCount }

    /**
     * full sha1 of current commit
     *
     * @see [currentSha1Short]
     */
    public val currentSha1: String? by lazy {
        try {
            requireWorkingGit()
            return@lazy gitInfoExtractor.currentSha1
        } catch (e: Throwable) {
            return@lazy null
        }
    }

    /**
     * 7 char sha1 of current commit
     */
    public val currentSha1Short: String? by lazy { currentSha1?.take(7) }

    /**
     * [yearFactor] based time component from initial commit to [featureBranchOriginCommit]
     */
    public val timeComponent: Int by lazy {
        try {
            requireWorkingGit()
            requireWorkingHistory()
            val latestBaseCommit = featureBranchOriginCommit ?: return@lazy 0

            val timeToHead = gitInfoExtractor.commitDate(latestBaseCommit) - gitInfoExtractor.initialCommitDate
            return@lazy (timeToHead * yearFactor / YEAR_IN_SECONDS + 0.5).toInt()
        } catch (e: Throwable) {
            // fallback
            return@lazy 0
        }
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
    public val initialCommit: String? by lazy {
        try {
            requireWorkingGit()
            requireWorkingHistory()
            gitInfoExtractor.commitsToHead.lastOrNull()
        } catch (e: Throwable) {
            return@lazy null
        }
    }

    /**
     * whether git can be used to extract data
     */
    public val isGitProjectCorrectlyInitialized: Boolean = gitInfoExtractor.isGitWorking

    @Suppress("unused")
    @Deprecated(message = "renamed", replaceWith = ReplaceWith("isGitProjectCorrectlyInitialized"))
    public val isGitInitialized
        get() = isGitProjectCorrectlyInitialized

    public val isHistoryShallowed: Boolean = gitInfoExtractor.isHistoryShallowed

    /**
     * commits of base branch in history of current commit (HEAD).
     */
    private val baseBranchCommits: List<String> by lazy {
        try {
            val baseCommits = gitInfoExtractor.commitsUpTo(baseBranch)
            val headCommits = gitInfoExtractor.commitsToHead

            return@lazy baseCommits.filter { it in headCommits }
        } catch (e: Throwable) {
            return@lazy emptyList<String>()
        }
    }

    /**
     * commits on the feature branch not merged into [baseBranch]
     */
    private val featureBranchCommits: List<String> by lazy {
        try {
            return@lazy gitInfoExtractor.commitsToHead.filter { !baseBranchCommits.contains(it) }
        } catch (e: Throwable) {
            return@lazy emptyList<String>()
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun requireWorkingGit() {
        if (!gitInfoExtractor.isGitWorking) throw IllegalStateException("Git is not working as expected. See logs for details")
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun requireWorkingHistory() {
        if (gitInfoExtractor.isHistoryShallowed) throw IllegalStateException("Shallow clone detected, no history available to execute action")
    }

    companion object {

        @JvmStatic
        public val DEFAULT_FORMATTER: ((GitVersioner) -> CharSequence) = { versioner ->
            with(versioner) {
                val sb = StringBuilder(if(isHistoryShallowed) "shallowed" else versioner.versionCode.toString())
                val hasCommits = featureBranchCommitCount > 0 || baseBranchCommitCount > 0
                if (baseBranch != branchName && (hasCommits || isHistoryShallowed)) {
                    // add branch identifier for
                    val shortName = try {
                        shortNameFormatter(versioner)
                    } catch (e: Throwable) {
                        println("shortNameFormatter failed to generate a correct name, using default formatter")
                        DEFAULT_SHORT_NAME_FORMATTER(versioner).toString()
                    }

                    sb.append("-").append(shortName)
                }

                val featureCount = featureBranchCommits.count()
                if (featureCount > 0 && !isHistoryShallowed) {
                    sb.append("+").append(featureCount)
                }
                if (localChanges != NO_CHANGES) {
                    if (addSnapshot) {
                        sb.append("-SNAPSHOT")
                    }
                    if (addLocalChangesDetails) {
                        sb.append("(").append(localChanges).append(")")
                    }
                }
                sb.toString()
            }
        }

        @JvmStatic
        public val DEFAULT_SHORT_NAME_FORMATTER: ((GitVersioner) -> CharSequence) = { versioner ->
            var name: String? = null
            if (name == null) {
                // use branch name from git
                val branchName = versioner.branchName
                if (branchName != null && branchName.isNotEmpty()) {
                    name = branchName
                }
            }
            if (name == null) {
                // nothing found fallback to sha1
                name = versioner.currentSha1Short
            }
            if (name == null) {
                // fallback, i.e. when git not initialized
                name = "undefined"
            }

            name.replace(Regex("(.*/)"), "")
        }
    }
}

public data class LocalChanges(
    val filesChanged: Int = 0,
    val additions: Int = 0,
    val deletions: Int = 0
) {

    override fun toString(): String {
        return "$filesChanged +$additions -$deletions"
    }

    fun shortStats(): String = if (filesChanged + additions + deletions == 0) {
        "no changes"
    } else {
        "files changed: $filesChanged, additions(+): $additions, deletions(-): $deletions"
    }
}
