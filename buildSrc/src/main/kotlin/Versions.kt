import kotlin.String

/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val com_jfrog_artifactory_gradle_plugin: String = "4.9.0" 

    const val mockito_kotlin: String = "1.6.0"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2" 

    const val junit: String = "4.12" 

    const val assertj_core: String = "3.11.1"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.10" // available: "1.3.11"

    const val org_jetbrains_kotlin: String = "1.3.10" // available: "1.3.11"

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "4.10"

        const val currentVersion: String = "5.1.1"

        const val nightlyVersion: String = "5.2-20190111000036+0000"

        const val releaseCandidate: String = ""
    }
}
