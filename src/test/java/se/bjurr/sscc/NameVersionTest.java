package se.bjurr.sscc;

import static se.bjurr.sscc.SsccPreReceiveRepositoryHook.getHookNameVersion;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class NameVersionTest {
 @Test
 public void testThatNameAndVersionCanBeReadFromProperties() throws IOException {
  refChangeBuilder().withHookNameVersion(getHookNameVersion())
    .withChangeSet(changeSetBuilder().withId("1").withMessage("SB-5678 fixing stuff").build()).build().run()
    .getOutputAll().startsWith("Simple Stash Commit Checker 1");
 }
}
