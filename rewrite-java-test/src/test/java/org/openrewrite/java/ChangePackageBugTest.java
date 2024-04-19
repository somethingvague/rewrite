package org.openrewrite.java;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.Issue;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpec;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;

@SuppressWarnings("ConstantConditions")
class ChangePackageBugTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ChangePackage("somepkg", "org.openrewrite.somepkg", null));
    }

    @Language("java")
    String testClassBefore = """
      package somepkg;
      public class Test extends Exception {
          public static void stat() {}
          public void foo() {}
      }
      """;

    @Language("java")
    String testClassAfter = """
      package org.openrewrite.somepkg;
      public class Test extends Exception {
          public static void stat() {}
          public void foo() {}
      }
      """;


    @Test
    void renameImport() {
        rewriteRun(
          java(
            """
              import somepkg.Test;
                            
              class A {
              }
              """,
            """
              import org.openrewrite.somepkg.Test;
                            
              class A {
              }
              """,
            spec -> spec.afterRecipe(cu -> {
                J.Import imported = cu.getImports().get(0);
                assertThat(imported.getPackageName()).isEqualTo("org.openrewrite.somepkg");
            })
          )
        );
    }


    @Test
    void updateVariableType() {
        rewriteRun(
          java(testClassBefore, testClassAfter),
          java(
            """
              import somepkg.Test;
                            
              public class A {
                  Test a;
              }
              """,
            """
              import org.openrewrite.somepkg.Test;
                            
              public class A {
                  Test a;
              }
              """,
            spec -> spec.afterRecipe(cu -> assertThat(TypeUtils.asFullyQualified(cu.getTypesInUse().getVariables().iterator().next().getType()).
              getFullyQualifiedName()).isEqualTo("org.openrewrite.somepkg.Test"))
          )
        );
    }

    @Test
    void renamePackage() {
        rewriteRun(
          java(
            """
              package somepkg;
              class Test {
              }
              """,
            """
              package org.openrewrite.somepkg;
              class Test {
              }
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.findType("somepkg.Test")).isEmpty();
                assertThat(cu.findType("org.openrewrite.somepkg.Test")).isNotEmpty();
            })
          )
        );
    }
}
