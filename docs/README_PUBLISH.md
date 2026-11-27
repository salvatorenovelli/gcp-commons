---
type: Runbook
title: "Publishing gcp-commons"
description: "How gcp-commons is published: a tag, the Cloud Build trigger, and the Maven repository in Artifact Registry that anyone can read."
---

A pushed tag `v*` starts the Cloud Build trigger `gcp-commons-publish`, which runs `cloudbuild.yaml`: it builds,
runs the tests and publishes the jar to the Maven repository `myseotoolbox-maven` in Artifact Registry
(`europe-west2`, project `my-seo-buddy`). The version is available as soon as the build ends.

Anyone can read the repository, so a consumer needs only its address:

```
repositories {
    mavenCentral()
    maven { url 'https://europe-west2-maven.pkg.dev/my-seo-buddy/myseotoolbox-maven' }
}
```

The versions up to 4.0.8 are on Maven Central, from the time the library was published through Sonatype.

To publish from a machine: `./gradlew publish`, with `gcloud` logged in to the project. `build.gradle` applies
Google's Gradle plugin only for that task, because the plugin looks for Google credentials when it is applied
([authentication](https://docs.cloud.google.com/artifact-registry/docs/java/authentication)).
