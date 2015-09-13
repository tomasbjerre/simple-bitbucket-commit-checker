package se.bjurr.sbcc;

import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_INC;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_JIRA_INC;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_GROUP_ACCEPT;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_GROUP_MESSAGE;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

import se.bjurr.sbcc.settings.SbccGroup;

public class GroupManyRuleManyTest {
 @Test
 public void testThatACommitCanBeRejectedByFirstOfTwoGroups() throws IOException {
  refChangeBuilder()
    .withGroupAcceptingOnlyJiraAndAnotherGroupAcceptingOnlyInc()
    .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA).build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas <my@email.com> >>> SB-5678 fixing stuff  - You need to specity INC   Incident, INC: INC")
    .wasRejected();
 }

 @Test
 public void testThatACommitCanBeRejectedByTwoGroups() throws IOException {
  refChangeBuilder()
    .withGroupAcceptingOnlyJiraAndAnotherGroupAcceptingOnlyInc()
    .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_INC).build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas <my@email.com> >>> SB-5678 fixing stuff  - You need to specity INC   Incident, INC: INC   3 Tomas <my@email.com> >>> INC123 correcting incident  - You need to specity JIRA   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)")
    .wasRejected();
 }

 @Test
 public void testThatItCanShowMessageFromOneOfTwoGroups() throws IOException {
  refChangeBuilder()
    .withGroupAcceptingOnlyJiraAndAnotherGroupAcceptingOnlyInc()
    .withSetting(SETTING_GROUP_ACCEPT + "[1]", SbccGroup.Accept.SHOW_MESSAGE.toString())
    .withSetting(SETTING_GROUP_MESSAGE + "[1]", "INC =)")
    .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_JIRA_INC).build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   3 Tomas <my@email.com> >>> SB-5678 INC123 fixing stuff  - INC =)")
    .wasAccepted();
 }

 @Test
 public void testThatItCanShowMessageFromOneOfTwoGroupsAndRejectedByOther() throws IOException {
  refChangeBuilder()
    .withGroupAcceptingOnlyJiraAndAnotherGroupAcceptingOnlyInc()
    .withSetting(SETTING_GROUP_ACCEPT + "[1]", SbccGroup.Accept.SHOW_MESSAGE.toString())
    .withSetting(SETTING_GROUP_MESSAGE + "[1]", "Nice inc =)")
    .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_INC).build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   3 Tomas <my@email.com> >>> INC123 correcting incident  - You need to specity JIRA   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)  - Nice inc =)")
    .wasRejected();
 }

 @Test
 public void testThatOneGroupCanAcceptAllMatchingJiraAndOneGroupRejectNoneMatchingQC() throws IOException {
  refChangeBuilder()
    .withGroupAcceptingJirasAndAnotherRejectingInc()
    .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_JIRA_INC).build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   3 Tomas <my@email.com> >>> SB-5678 INC123 fixing stuff  - Dont include INC   Incident, INC: INC")
    .wasRejected();
 }

 @Test
 public void testThatOneGroupCanAcceptAllMatchingJiraAndOneGroupRejectNoneMatchingQCPartly() throws IOException {
  refChangeBuilder()
    .withGroupAcceptingJirasAndAnotherRejectingInc()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA_INC).build())
    .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_INC).build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas <my@email.com> >>> SB-5678 INC123 fixing stuff  - Dont include INC   Incident, INC: INC   3 Tomas <my@email.com> >>> INC123 correcting incident  - You need to specity JIRA   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)  - Dont include INC   Incident, INC: INC")
    .wasRejected();
 }
}
