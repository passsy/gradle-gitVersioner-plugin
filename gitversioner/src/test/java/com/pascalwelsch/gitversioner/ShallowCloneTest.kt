package com.pascalwelsch.gitversioner

import org.assertj.core.api.Assertions.*
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.*
import org.junit.runner.*
import org.junit.runners.*
import java.util.concurrent.TimeUnit

@RunWith(JUnit4::class)
class ShallowCloneTest {

    @Test
    fun `default - clean on default branch master`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "j", date = 150_010_000) // <-- master, HEAD
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "master"), isHistoryShallowed = true)
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isNull()
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `default - with local changes - addSnapshot false`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "j", date = 150_010_000) // <-- master, HEAD
        )

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("X" to "master"), localChanges, isHistoryShallowed = true)
        val versioner = GitVersioner(git)
        versioner.addSnapshot = false

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed(3 +5 -7)")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isNull()
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `default - with local changes - addLocalChangesDetails false`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "j", date = 150_010_000) // <-- master, HEAD
        )
        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("X" to "master"), localChanges, isHistoryShallowed = true)
        val versioner = GitVersioner(git)
        versioner.addLocalChangesDetails = false

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed-SNAPSHOT")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isNull()
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `default - without local changes information`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "j", date = 150_010_000) // <-- master, HEAD
        )

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("X" to "master"), localChanges, isHistoryShallowed = true)
        val versioner = GitVersioner(git)
        versioner.addLocalChangesDetails = false
        versioner.addSnapshot = false

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isNull()
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `default - with local changes`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "j", date = 150_010_000) // <-- master, HEAD
        )

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("X" to "master"), localChanges, isHistoryShallowed = true)
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed-SNAPSHOT(3 +5 -7)")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isNull()
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `base branch not in history`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "j", date = 150_010_000) // <-- master, HEAD
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "master"), isHistoryShallowed = true)
        val versioner = GitVersioner(git).apply {
            baseBranch = "develop"
        }

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed-master")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("develop")
            softly.assertThat(versioner.initialCommit).isNull()
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)

            // no commit in both HEAD history and develop because develop is not part of the graph
            softly.assertThat(versioner.featureBranchOriginCommit).isNull()
        }
    }

    @Test
    fun `on orphan initial commit`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = null, date = 150_010_000) // <-- HEAD, orphan
        )

        val git = MockGitRepo(graph, "X", branchHeads = listOf("X" to "orphan"), isHistoryShallowed = true)
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed-orphan")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("orphan")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isNull()
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isNull()
        }
    }

    @Test
    fun `on orphan few commits`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "b'", date = 150_030_000) // <-- HEAD, feature/x
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "feature/x"), isHistoryShallowed = true)
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed-x")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("feature/x")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isNull()
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isNull()
        }
    }

    @Test
    fun `first commit - no parent`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = null, date = 150_006_000) // <-- master, HEAD
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "master"), isHistoryShallowed = true)
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isNull()
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `short sha1`() {
        val graph = listOf(
            Commit(sha1 = "abcdefghijkl", parent = null, date = 150_006_000) // <-- master, HEAD
        )

        val git = MockGitRepo(graph, "abcdefghijkl", listOf("abcdefghijkl" to "master"), isHistoryShallowed = true)
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("abcdefghijkl")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.initialCommit).isNull()
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("abcdefghijkl")
        }

        assertThat(versioner.currentSha1Short).isEqualTo("abcdefg").hasSize(7)
    }

    @Test
    fun `no commits`() {
        val git = MockGitRepo(isHistoryShallowed = true) // git initialized but nothing committed
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed-undefined")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isNull()
            softly.assertThat(versioner.currentSha1).isNull()
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isNull()
        }
    }

    @Test
    fun `no branch name - sha1 fallback`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "j", date = 150_010_000) // <-- HEAD
        )

        val git = MockGitRepo(graph, "X", isHistoryShallowed = true)
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed-X")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isNull()
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isNull()
        }
    }

    @Test
    fun `default - branchname from ci`() {
        val graph = listOf(
            Commit(sha1 = "X", parent = "j", date = 150_010_000) // <-- HEAD, master
        )

        val git = object : MockGitRepo(graph, "X", listOf("X" to "master"), isHistoryShallowed = true) {
            // explicitly checked out sha1 not branch
            override val currentBranch: String? = null
        }
        val versioner = GitVersioner(git)
        versioner.ciBranchNameProvider = { "nameFromCi" }

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode).isEqualTo(1)
            softly.assertThat(versioner.versionName).isEqualTo("shallowed-nameFromCi")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("nameFromCi")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

}