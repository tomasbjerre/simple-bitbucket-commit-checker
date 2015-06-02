package se.bjurr.sscc;

import static com.atlassian.stash.user.UserType.SERVICE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_ALLOW_SERVICE_USERS;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class ServiceUserTest {

 @Test
 public void testThatServiceUserIsAccepted() throws IOException {
  refChangeBuilder()
    .withStashUserType(SERVICE)
    .withSetting(SETTING_ALLOW_SERVICE_USERS, TRUE)
    .withGroupAcceptingAtLeastOneJira()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> fixing stuff  - You need to specity an issue   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)")
    .wasAccepted();
 }

 @Test
 public void testThatNormalUserIsRejected() throws IOException {
  refChangeBuilder().withStashUserType(SERVICE).withSetting(SETTING_ALLOW_SERVICE_USERS, FALSE)
    .withGroupAcceptingAtLeastOneJira()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build()).build().run()
    .wasRejected();
 }
}
