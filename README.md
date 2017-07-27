# Git Versioner for gradle

Version numbers are hard. 
It was easier with SVN where the revision number got increased for every commit. 
Revision `342` was clearly older than revision `401`. 
This is not possible in git because branching is so common (and that's a good thing). 
`342` commits could mean multiple commits on different branches.
Not even the latest common commit in history is clear.
This projects aims to bring the SVN simplicity and more back to git for your gradle (android) project.

## Install

```gradle
// Top-level build.gradle
buildscript {
    dependencies {
        classpath 'com.pascalwelsch.gitversioner:gitversioner:0.3.1'
    }
}

apply plugin: 'com.pascalwelsch.gitversioner'

gitVersioner {
    baseBranch "develop"
    //TODO add more
}
```


```gradle
// app module build.gradle

android {

    defaultConfig {
        versionCode gitVersioner.versionCode
        versionName gitVersioner.versionName
    }
}
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
