# How to perform a release

Releases are published through the Maven Central Publisher Portal. Sign in and
open <https://central.sonatype.com/usertoken>, then:

1. Select **Generate User Token**.
2. Give the token a descriptive name and choose an expiration date.
3. Save the generated username and password immediately. They cannot be
   retrieved after the dialog is closed; generate a replacement if they are
   lost.

Add the generated credentials to `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>token username</username>
      <password>token password</password>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>jheaps-signing</id>
      <properties>
        <gpg.keyname>YOUR_KEY_FINGERPRINT</gpg.keyname>
      </properties>
    </profile>
  </profiles>

  <activeProfiles>
    <activeProfile>jheaps-signing</activeProfile>
  </activeProfiles>
</settings>
```

Replace `YOUR_KEY_FINGERPRINT` with the fingerprint of the signing key available
to GnuPG. Prime `gpg-agent` interactively before starting the Maven release.
This keeps the passphrase out of the POM, Maven settings, environment, and shell
history:

```shell
gpg --local-user YOUR_KEY_FINGERPRINT --sign </dev/null >/dev/null
mvn release:prepare
mvn release:perform
```

The release profile signs the artifacts and uploads the deployment bundle to
the Central Publisher Portal. After validation succeeds, review and publish the
deployment at <https://central.sonatype.com/publishing/deployments>.
