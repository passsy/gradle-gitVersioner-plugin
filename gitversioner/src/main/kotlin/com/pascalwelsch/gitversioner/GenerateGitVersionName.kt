package com.pascalwelsch.gitversioner

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.util.Properties

internal open class GenerateGitVersionName : DefaultTask() {

    internal lateinit var gitVersioner: GitVersioner

    @TaskAction
    fun generate() {
        val path = "${project.buildDir}/gitversion/gitversion.properties"
        val file = project.file("${project.buildDir}/gitversion/version.properties")
        file.parentFile.mkdirs()

        val properties = Properties().apply {
            with(gitVersioner) {
                putWhenSet("versionCode", versionCode)
                putWhenSet("versionName", versionName)
                putWhenSet("baseBranch", baseBranch)
                putWhenSet("branchName", branchName)
                putWhenSet("currentSha1", currentSha1)
                putWhenSet("baseBranchCommitCount", baseBranchCommitCount)
                putWhenSet("featureBranchCommitCount", featureBranchCommitCount)
                putWhenSet("timeComponent", timeComponent)
                putWhenSet("yearFactor", yearFactor)
                putWhenSet("localChanges", localChanges)
            }
        }

        properties.store(file.writer(), "gitVersioner plugin - extracted data from git repository")
        project.logger.lifecycle("git versionName: ${gitVersioner.versionName}")
        project.logger.lifecycle("gitVersion output: $path")
    }
}

private fun Properties.putWhenSet(key: String, value: Any?) {
    if (value == null) return
    put(key, value.toString())
}