package se.bjurr.sscc;

import static org.mockito.Mockito.mock;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_BRANCHES;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class ExceptionTest {
 private Logger beforeLogger;

 @Before
 public void before() {
  beforeLogger = SsccPreReceiveRepositoryHook.getLogger();
  Logger mockLogger = mock(Logger.class);
  SsccPreReceiveRepositoryHook.setLogger(mockLogger);
 }

 @After
 public void after() {
  SsccPreReceiveRepositoryHook.setLogger(beforeLogger);
 }

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
