package se.bjurr.sbcc;

import static com.atlassian.bitbucket.user.UserType.SERVICE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_ALLOW_SERVICE_USERS;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_IGNORE_USERS_PATTERN;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;
import org.junit.Test;

public class UserTest {

  @Test
  public void testThatServiceUserIsAccepted() throws IOException {
    refChangeBuilder() //
        .withBitbucketUserType(SERVICE) //
        .withSetting(SETTING_ALLOW_SERVICE_USERS, TRUE) //
        .withGroupAcceptingAtLeastOneJira() //
        .withChangeSet( //
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE)
                .build() //
            ) //
        .build() //
        .run() //
        .wasAccepted();
  }

  @Test
  public void testThatUserIsAcceptedIfMatchingPattern() throws IOException {
    refChangeBuilder() //
        .withBitbucketName("ADMIN-Tomas") //
        .withSetting(SETTING_IGNORE_USERS_PATTERN, "^ADMIN.*") //
        .withGroupAcceptingAtLeastOneJira() //
        .withChangeSet( //
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE)
                .build() //
            ) //
        .build() //
        .run() //
        .wasAccepted();
  }

  @Test
  public void testThatUserIsRejectedIfNotMatchingPattern() throws IOException {
    refChangeBuilder() //
        .withBitbucketUserType(SERVICE) //
        .withBitbucketName("Tomas") //
        .withSetting(SETTING_IGNORE_USERS_PATTERN, "^ADMIN") //
        .withGroupAcceptingAtLeastOneJira() //
        .withChangeSet( //
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE)
                .build() //
            ) //
        .build() //
        .run() //
        .wasRejected();
  }

  @Test
  public void testThatNormalUserIsRejected() throws IOException {
    refChangeBuilder() //
        .withBitbucketUserType(SERVICE) //
        .withSetting(SETTING_ALLOW_SERVICE_USERS, FALSE) //
        .withGroupAcceptingAtLeastOneJira() //
        .withChangeSet( //
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
                .build() //
            ) //
        .build() //
        .run() //
        .wasRejected();
  }
}
