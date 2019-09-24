package com.pascalwelsch.gitversioner

import org.assertj.core.api.Assertions.*
import org.junit.*
import org.junit.runner.*
import org.junit.runners.*

@RunWith(JUnit4::class)
class PrefixedBranchTest {

    @Test
    fun `replace special characters`() {
        assertThat(versionNameForBranch("craz+y-n=,a^m;e")).isEqualTo("3-craz_y-n_a_m_e")
    }

    @Test
    fun `remove prefix + replace special characters`() {
        assertThat(versionNameForBranch("feature/craz+y-n=,a^m;e")).isEqualTo("3-craz_y-n_a_m_e")
    }

    @Test
    fun `remove prefix`() {
        assertThat(versionNameForBranch("bugfix/bug_123")).isEqualTo("3-bug_123")
    }

    @Test
    fun `remove only first prefix`() {
        assertThat(versionNameForBranch("bugfix/something/bug_123")).isEqualTo("3-something_bug_123")
    }
}

private fun versionNameForBranch(branchName: String): String {
    val versioner = GitVersioner(GitInfoExtractorStub(commits = listOf("a", "b", "c"), currentBranch = branchName))
    return versioner.versionName
}
