


GitHub action `.github/workflows/gradle-publish.yml`

Secrets are stored in ~/.gradle/gradle.properties
to extract key to copy into github secrets `gpg --export-secret-keys --armor XXXXXXX > local.asc`

To publish `./gradlew clean build sign publish`

Repo is: `https://s01.oss.sonatype.org/#welcome`


