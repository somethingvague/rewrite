Just a fork to demonstrate probable bug in the `ChangePackage` recipe.

Reproduced with:
```bash
./gradlew -q :rewrite-java-test:test --tests org.openrewrite.java.ChangePackageBugTest
```