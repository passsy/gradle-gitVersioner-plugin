package com.pascalwelsch.gitversioner

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File

interface GitInfoExtractor {
    val currentSha1: String?
    val currentBranch: String?
    val localChanges: LocalChanges
    val initialCommitDate: Long
    val commitsToHead: List<String>
    val isGitWorking: Boolean
    val isHistoryShallowed: Boolean
    fun commitDate(rev: String): Long
    fun commitsUpTo(rev: String, args: String = ""): List<String>
}

/**
 * Executes shell commands to get information from git
 */
internal class ShellGitInfoExtractor(private val project: Project) : GitInfoExtractor {

    override val currentSha1: String? by lazy {
        val sha1 = "git rev-parse HEAD".execute().throwOnError().text.trim()
        if (sha1.isEmpty()) null else sha1
    }

    override val currentBranch: String? by lazy {
        when (val result = "git symbolic-ref --short -q HEAD".execute()) {
            is ProcessResult.Success -> {
                val branch = result.text.trim()
                if (branch.isEmpty()) null else branch
            }
            is ProcessResult.Error -> null
        }
    }

    override val localChanges: LocalChanges by lazy {
        val shortStat = "git diff HEAD --shortstat".execute().throwOnError().text.trim()
        if (shortStat.isEmpty()) return@lazy NO_CHANGES

        return@lazy parseShortStats(shortStat)
    }

    override val initialCommitDate: Long by lazy {
        val initialCommit: String = commitsToHead.lastOrNull() ?: return@lazy 0L
        val time = listOf("git", "log", "-n 1", "--pretty=format:'%at'", initialCommit).execute()
            .throwOnError().text.replace("\'", "").trim()

        return@lazy if (time.isEmpty()) 0L else time.toLong()
    }

    override fun commitDate(rev: String): Long {
        val time = listOf("git", "log", "--pretty=format:'%at'", "-n 1", rev).execute()
            .throwOnError().text.replace("\'", "").trim()
        return if (time.isEmpty()) 0 else time.toLong()
    }

    override val commitsToHead: List<String> by lazy { commitsUpTo("HEAD") }

    override val isGitWorking: Boolean by lazy {
        val result = "git status".execute()
        if (result is ProcessResult.Error) {
            when (val exitCode = result.errorCode) {
                69 -> {
                    println(
                        "git returned with error 69\n" +
                                "If you are a mac user that message is telling you is that you need to open the " +
                                "application XCode on your Mac OS X/macOS and since it hasn’t run since the last " +
                                "update, you need to accept the new license EULA agreement that’s part of the " +
                                "updated XCode.\n\n" +
                                "tl;dr run\n" +
                                "\txcode-select --install"
                    )
                    return@lazy false
                }
                else -> {
                    println("ERROR: can't generate a git version, this is not a git project. git status errors with error code: $exitCode")
                    println(" -> Not a git repository (or any of the parent directories): .git")
                    return@lazy false
                }
            }
        }

        return@lazy true
    }

    override val isHistoryShallowed: Boolean by lazy {
        // returns root dir of git
        val rootPath = "git rev-parse --show-toplevel".execute().throwOnError().text.trim()
        val shallowFile = File("$rootPath/.git/shallow")

        if (shallowFile.exists()) {
            println(
                "WARNING: Git history is incomplete\n" +
                        "The gradle git version plugin requires the complete git history to calculate " +
                        "the version. The history is shallowed, therefore the version code would be incorrect.\n" +
                        "Please fetch the complete history with:\n" +
                        "\tgit fetch --unshallow"
            )
            return@lazy true
        }

        return@lazy false
    }

    override fun commitsUpTo(rev: String, args: String): List<String> {
        val text = try {
            "git rev-list $rev $args".execute().throwOnError().text
        } catch (e: Exception) {
            try {
                "git rev-list origin/$rev $args".execute().throwOnError().text
            } catch (e: Exception) {
                ""
            }
        }

        return text.lines().asSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
    }

    private fun String.execute(): ProcessResult = trim().split(" ").execute()

    private fun List<String>.execute(): ProcessResult {
        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val task = project.exec {
            it.commandLine = this@execute
            it.standardOutput = out
            it.errorOutput = err
            it.workingDir = project.projectDir
            it.isIgnoreExitValue = true
        }
        val exitCode = task.exitValue
        return if (exitCode == 0) {
            ProcessResult.Success(out.toString())
        } else {
            ProcessResult.Error(err.toString(), exitCode, this.joinToString(" "))
        }
    }

    private sealed class ProcessResult {
        class Success(val text: String) : ProcessResult()
        class Error(val text: String, val errorCode: Int, val command: String) : ProcessResult() {
            fun errorMessage(): String = "Error $errorCode executing `$command`\n\n${text.take(100)}"
        }

        fun throwOnError(): Success {
            when (this) {
                is Success -> return this
                is Error -> throw GradleException(this.errorMessage())
            }
        }
    }
}

/**
 * parses `git diff --shortstat`
 *
 * https://github.com/git/git/blob/69e6b9b4f4a91ce90f2c38ed2fa89686f8aff44f/diff.c#L1561
 */
internal fun parseShortStats(shortstat: String): LocalChanges {
    val parts = shortstat.split(",")

    var filesChanges = 0
    var additions = 0
    var deletions = 0

    parts.map { it.trim() }.forEach { part ->
        if (part.contains("changed")) {
            val matches: MatchResult? = "(\\d+).*".toRegex().find(part)
            if (matches != null && matches.groups.size >= 2) {
                filesChanges = matches.groupValues[1].toInt()
            }
        }
        if (part.contains("(+)")) {
            val matches: MatchResult? = "(\\d+).*".toRegex().find(part)
            if (matches != null && matches.groups.size >= 2) {
                additions = matches.groupValues[1].toInt()
            }
        }
        if (part.contains("(-)")) {
            val matches: MatchResult? = "(\\d+).*".toRegex().find(part)
            if (matches != null && matches.groups.size >= 2) {
                deletions = matches.groupValues[1].toInt()
            }
        }
    }

    return LocalChanges(filesChanges, additions, deletions)
}
