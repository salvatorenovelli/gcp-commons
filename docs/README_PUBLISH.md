---
type: Runbook
title: "Publishing gcp-commons"
description: "Notes on publishing gcp-commons: the GitHub action, where the secrets are, the publish command and the repository."
---



GitHub action `.github/workflows/gradle-publish.yaml`: a pushed tag `v*` builds, publishes to the staging
repository and uploads it to the Central Portal with `publishing_type=automatic`, so Sonatype releases it to Maven
Central when it passes validation. Without that parameter the deployment waits at
`https://central.sonatype.com/publishing` for a person to publish it
([OSSRH Staging API](https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/)).

Secrets are stored in ~/.gradle/gradle.properties
to extract key to copy into github secrets `gpg --export-secret-keys --armor XXXXXXX > local.asc`

To publish `./gradlew clean build sign publish`

Repo is: `https://s01.oss.sonatype.org/#welcome`


