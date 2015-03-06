package se.bjurr.sscc;

import static se.bjurr.sscc.settings.SSCCSettings.SETTING_BRANCHES;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class ExceptionTest {

 @Test
 public void testThatRefChangesAreAcceptedIfAnUnexpectedExceptionIsThrown() throws IOException {
  refChangeBuilder()
    .withHookNameVersion("Simple Stash Commit Checker X.X")
    .throwing(new IOException("the error"))
    .run()
    .wasAccepted()
    .hasTrimmedFlatOutput(
      "Simple Stash Commit Checker X.X  Error while validating reference changes. Will allow all of them. \"the error\"");
 }

 /**
  * Should no be possible to store invalid settings. But it would be a disaster
  * if such settings somehow were stored.
  */
 @Test
 public void testThatRefChangesAreAcceptedIfInvalidSettingsAreSet() throws IOException {
  refChangeBuilder()
    .withHookNameVersion("Simple Stash Commit Checker X.X")
    .withSetting(SETTING_BRANCHES, "[notok")
    .build()
    .run()
    .wasAccepted()
    .hasTrimmedFlatOutput(
      "Simple Stash Commit Checker X.X  Error while validating reference changes. Will allow all of them. \"branches=Invalid Regexp: Unclosed character class near index 5 [notok      ^\"");
 }
}
