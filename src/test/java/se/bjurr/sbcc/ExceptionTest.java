package se.bjurr.sbcc;

import static org.mockito.Mockito.mock;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_BRANCHES;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExceptionTest {
 private Logger beforeLogger;

 @Before
 public void before() {
  beforeLogger = SbccPreReceiveRepositoryHook.getLogger();
  Logger mockLogger = mock(Logger.class);
  SbccPreReceiveRepositoryHook.setLogger(mockLogger);
 }

 @After
 public void after() {
  SbccPreReceiveRepositoryHook.setLogger(beforeLogger);
 }

 @Test
 public void testThatRefChangesAreAcceptedIfAnUnexpectedExceptionIsThrown() throws IOException {
  refChangeBuilder().withHookNameVersion("Simple Bitbucket Commit Checker X.X").throwing(new IOException("the error"))
    .run().wasAccepted().hasTrimmedFlatOutput(
      "Simple Bitbucket Commit Checker X.X  Error while validating reference changes. Will allow all of them. \"the error\"");
 }

 /**
  * Should no be possible to store invalid settings. But it would be a disaster
  * if such settings somehow were stored.
  */
 @Test
 public void testThatRefChangesAreAcceptedIfInvalidSettingsAreSet() throws IOException {
  refChangeBuilder().withHookNameVersion("Simple Bitbucket Commit Checker X.X").withSetting(SETTING_BRANCHES, "[notok")
    .build().run().wasAccepted().hasTrimmedFlatOutput(
      "Simple Bitbucket Commit Checker X.X  Error while validating reference changes. Will allow all of them. \"branches=Invalid Regexp: Unclosed character class near index 5 [notok      ^\"");
 }
}
