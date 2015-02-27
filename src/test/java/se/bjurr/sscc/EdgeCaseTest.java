package se.bjurr.sscc;

import static se.bjurr.sscc.settings.SSCCSettings.SETTING_BRANCHES;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class EdgeCaseTest {

 @Test
 public void testThatRefChangesAreAcceptedIfAnUnexpectedExceptionIsThrown() throws IOException {
  refChangeBuilder()
    .throwing(new IOException("the error"))
    .run()
    .wasAccepted()
    .hasTrimmedFlatOutput(
      "Simple Stash Commit Checker> Error while validating reference changes. Will allow all of them. \"the error\"");
 }

 /**
  * Should no be possible to store invalid settings. But it would be a disaster
  * if such settings somehow were stored.
  */
 @Test
 public void testThatRefChangesAreAcceptedIfInvalidSettingsAreSet() throws IOException {
  refChangeBuilder()
    .withSetting(SETTING_BRANCHES, "[notok")
    .build()
    .run()
    .wasAccepted()
    .hasTrimmedFlatOutput(
      "Simple Stash Commit Checker> Error while validating reference changes. Will allow all of them. \"branches=Invalid Regexp: Unclosed character class near index 5 [notok      ^\"");
 }
}
