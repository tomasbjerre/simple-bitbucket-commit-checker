package se.bjurr.sbcc;

import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class NameVersionTest {
 @Test
 public void testThatNameAndVersionIsPrinted() throws IOException {
  refChangeBuilder().withHookNameVersion("Simple Bitbucket Commit Checker")
    .withChangeSet(changeSetBuilder().withId("1").withMessage("SB-5678 fixing stuff").build()).build().run()
    .getOutputAll().startsWith("Simple Bitbucket Commit Checker");
 }
}
