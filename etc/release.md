
# How to perform a release 

Open ~/.m2/settings.xml and add

<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>myusername</username>
      <password>mypassword</password>
    </server>

    <server>
      <id>gpg.passphrase</id>
      <passphrase>clear or encrypted text</passphrase>
    </server>

  </servers>
</settings>

Make sure you have an account at the staging repositories at https://oss.sonatype.org/#stagingRepositories .
Also make sure that you have your gpg key imported in your keyring.

Execute the following:
```
mvn release:prepare
```

If it succeeds then: 

```
mvn release:perform
```

Go to https://oss.sonatype.org/#stagingRepositories and after validating that the files are correct, you 
need to first close the opened staged repository and then release it.

