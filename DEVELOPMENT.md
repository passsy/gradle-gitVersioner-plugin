# Development 

## Testing the gradle plugin in a real project

1. Add jar dependency to real project

`build.gradle`
```gradle
buildscript {
    dependencies {
        classpath(files("/path/to/gradle-gitVersioner-plugin/gitversioner/build/libs/gitversioner-0.4.3.jar"))
    }
}
``` 

2. build the plugin
```bash
./gradlew clean build
```

3. build the real project

```bash
./gradlew clean gitVersion
```

## Publish artifacts

Add bintray keys to `~/.gradle/gradle.properties`
```properties
bintrayUser=passsy
bintrayKey=cba6a4869abc49b6ca4...
```

Publish
```bash
 ./gradlew clean build bintrayUpload -PdryRun=false
```