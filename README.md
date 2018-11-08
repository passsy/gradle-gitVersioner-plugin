# Git Versioner for gradle

Version numbers are hard. 
It was easier with SVN where the revision number got increased for every commit. 
Revision `342` was clearly older than revision `401`. 
This is not possible in git because branching is so common (and that's a good thing). 
`342` commits could mean multiple commits on different branches.
Not even the latest common commit in history is clear.
This projects aims to bring the SVN simplicity and more back to git for your gradle (android) project.

## Example

On `master` branch with `1850` commits and roughly half a year of development (`526` timecomponent)
```
versionName 2376
versionCode 2376
```

On your feature branch `feature/upgrade_gradle_4.4.1` with `3` additional commits/
`versionCode` stays the same and only reflects the base version on the baseBranch (`master`).
```
versionName 2376-upgrade_gradle_4.4.1+3
versionCode 2376
```

While working on your features when you have uncommited changes `files changed: 1, additions(+): 2, deletions(-): 0`.
```
versionName 2376-upgrade_gradle_4.4.1+3-SNAPSHOT(1 +2 -0)
versionCode 2376
```

## Install

```gradle
// Top-level build.gradle
buildscript {
    dependencies {
        classpath 'com.pascalwelsch.gitversioner:gitversioner:0.4.0'
    }
}

// https://github.com/passsy/gradle-gitVersioner-plugin
apply plugin: 'com.pascalwelsch.gitversioner'
gitVersioner {
    baseBranch 'develop'
}
```

## Usage Android

```gradle
// app module build.gradle

android {

    defaultConfig {
        versionCode gitVersioner.versionCode
        versionName gitVersioner.versionName
    }
}
```

## Additional gitVersioner properties

Here are the default properties, you can change them. See [here](https://github.com/passsy/gradle-gitVersioner-plugin/blob/master/gitversioner/src/main/kotlin/com/pascalwelsch/gitversioner/GitVersioner.kt) for more details

```gradle 
gitVersioner {
    baseBranch 'master'
    yearFactor 1000 // increasing every 8.57h
    
    // Default formatter properties
    addSnapshot true // the "-SNAPSHOT" postfix
    addLocalChangesDetails true
    
    // provide a custom formatter
    formatter = { gitVersioner ->
        return "${gitVersioner.versionCode} custom generated id"
    }
}

```

## Gradle Tasks

### Show version output info
```
> ./gradlew gitVersion

GitVersioner Plugin
-------------------
VersionCode: 2118
VersionName: 2118-SNAPSHOT(1 +9 -8)

baseBranch: develop
current branch: develop
current commit: 1dd547a

baseBranch commits: 1653 (423e113..1dd547a)
featureBranch commits: 0 (1dd547a..1dd547a)

timeComponent: 465 (yearFactor:1000)

LocalChanges: files changed: 1, additions(+): 9, deletions(-): 8
```

### write gitVersion information properties file (machine readable)

```
> ./gradlew generateGitVersionName
git versionName: 2118-SNAPSHOT(1 +9 -8)
gitVersion output: build/gitversion/gitversion.properties
```

`gitversion.properties`
```
#gitVersioner plugin - extracted data from git repository
#Sun Jan 14 22:08:34 CET 2018
localChanges=1 +9 -8
timeComponent=465
versionName=2118-SNAPSHOT(1 +9 -8)
versionCode=2118
featureBranchCommitCount=0
baseBranchCommitCount=1653
baseBranch=develop
branchName=develop
yearFactor=1000
currentSha1=1dd547a910b1a1da64fd65ec4b4294030511be4b
```


# License

```
Copyright 2017 Pascal Welsch

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
