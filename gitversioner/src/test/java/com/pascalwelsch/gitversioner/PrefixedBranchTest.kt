package com.pascalwelsch.gitversioner

import org.assertj.core.api.SoftAssertions
import org.junit.*
import org.junit.runner.*
import org.junit.runners.*

@RunWith(JUnit4::class)
class PrefixedBranchTest {

    @Test
    fun `on branch with one prefix - few commits - local changes`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "j", date = 150_010_000), // <-- bugfix/bug_123, HEAD
            Commit(sha1 = "j", parent = "i", date = 150_009_000),
            Commit(sha1 = "i", parent = "h", date = 150_008_000),
            Commit(sha1 = "h", parent = "g", date = 150_007_000),
            Commit(sha1 = "g", parent = "f", date = 150_006_000), // <-- master
            Commit(sha1 = "f", parent = "e", date = 150_005_000),
            Commit(sha1 = "e", parent = "d", date = 150_004_000),
            Commit(sha1 = "d", parent = "c", date = 150_003_000),
            Commit(sha1 = "c", parent = "b", date = 150_002_000),
            Commit(sha1 = "b", parent = "a", date = 150_001_000),
            Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("g" to "master", "X" to "bugfix/bug_123"), localChanges)
        val versioner = GitVersioner(git)

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(7)
            softly.assertThat(versioner.versionName).isEqualTo("7-bug_123+4-SNAPSHOT(3 +5 -7)")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(7)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(4)
            softly.assertThat(versioner.branchName).isEqualTo("bugfix/bug_123")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isEqualTo("a")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("g")
        }
    }

    @Test
    fun `on branch with multiple prefixes - few commits - local changes`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "j", date = 150_010_000), // <-- bugfix/something/bug_123, HEAD
            Commit(sha1 = "j", parent = "i", date = 150_009_000),
            Commit(sha1 = "i", parent = "h", date = 150_008_000),
            Commit(sha1 = "h", parent = "g", date = 150_007_000),
            Commit(sha1 = "g", parent = "f", date = 150_006_000), // <-- master
            Commit(sha1 = "f", parent = "e", date = 150_005_000),
            Commit(sha1 = "e", parent = "d", date = 150_004_000),
            Commit(sha1 = "d", parent = "c", date = 150_003_000),
            Commit(sha1 = "c", parent = "b", date = 150_002_000),
            Commit(sha1 = "b", parent = "a", date = 150_001_000),
            Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("g" to "master", "X" to "bugfix/something/bug_123"), localChanges)
        val versioner = GitVersioner(git)

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(7)
            softly.assertThat(versioner.versionName).isEqualTo("7-bug_123+4-SNAPSHOT(3 +5 -7)")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(7)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(4)
            softly.assertThat(versioner.branchName).isEqualTo("bugfix/something/bug_123")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isEqualTo("a")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("g")
        }
    }
}