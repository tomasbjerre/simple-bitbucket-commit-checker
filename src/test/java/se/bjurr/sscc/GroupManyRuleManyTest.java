package se.bjurr.sscc;

import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_ACCEPT;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_MESSAGE;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

import se.bjurr.sscc.settings.SSCCGroup;

public class GroupManyRuleManyTest {
 @Test
 public void testThatACommitCanBeRejectedByFirstOfTwoGroups() throws IOException {
  refChangeBuilder()
  .withAllAcceptInTwoGroups()
  .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA).build())
  .build()
  .run()
  .hasTrimmedFlatOutput(
    "refs/heads/master e2bc4ed003 -> af35d5c1a4  2 Tomas <my@email.com> >>> SB-5678 fixing stuff You need to specity INC * INC   Incident, INC")
    .wasRejected();
 }

 @Test
 public void testThatACommitCanBeRejectedBySecondOfTwoGroups() throws IOException {
  refChangeBuilder()
  .withAllAcceptInTwoGroups()
  .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA).build())
  .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_NO_ISSUE + " INC3").build())
  .build()
  .run()
  .hasTrimmedFlatOutput(
    "refs/heads/master e2bc4ed003 -> af35d5c1a4  2 Tomas <my@email.com> >>> SB-5678 fixing stuff You need to specity INC * INC   Incident, INC  3 Tomas <my@email.com> >>> fixing stuff INC3 You need to specity JIRA * ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)   JIRA")
    .wasRejected();
 }

 @Test
 public void testThatACommitShowMessageFromFirstOfTwoGroups() throws IOException {
  refChangeBuilder()
  .withAllAcceptInTwoGroups()
  .withSetting(SETTING_GROUP_ACCEPT + "[1]", SSCCGroup.Accept.SHOW_MESSAGE.toString())
  .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_NO_ISSUE + " INC3").build())
  .build()
  .run()
  .hasTrimmedFlatOutput(
    "refs/heads/master e2bc4ed003 -> af35d5c1a4  3 Tomas <my@email.com> >>> fixing stuff INC3 You need to specity JIRA * ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)   JIRA You need to specity INC")
    .wasRejected();
 }

 @Test
 public void testThatACommitShowMessageFromFirstOfTwoGroupsAndRejectedBySecond() throws IOException {
  refChangeBuilder()
  .withAllAcceptInTwoGroups()
  .withSetting(SETTING_GROUP_ACCEPT + "[1]", SSCCGroup.Accept.SHOW_MESSAGE.toString())
  .withSetting(SETTING_GROUP_MESSAGE + "[1]", "INC =)")
  .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA).build())
  .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_JIRA + " INC3").build())
  .build()
  .run()
  .hasTrimmedFlatOutput(
    "refs/heads/master e2bc4ed003 -> af35d5c1a4  3 Tomas <my@email.com> >>> SB-5678 fixing stuff INC3 INC =)")
    .wasAccepted();
 }

 @Test
 public void testThatOneGroupCanAcceptAllMatchingJiraAndOneGroupRejectNoneMatchingQC() throws IOException {
  refChangeBuilder()
  .withAllAcceptJIRANoneAcceptINC()
  .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_JIRA + " INC3").build())
  .build()
  .run()
  .hasTrimmedFlatOutput(
    "refs/heads/master e2bc4ed003 -> af35d5c1a4  3 Tomas <my@email.com> >>> SB-5678 fixing stuff INC3 Dont include INC * INC   Incident, INC")
    .wasRejected();
 }

 @Test
 public void testThatOneGroupCanAcceptAllMatchingJiraAndOneGroupRejectNoneMatchingQCPartly() throws IOException {
  refChangeBuilder()
  .withAllAcceptJIRANoneAcceptINC()
  .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA).build())
  .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA + " INC3").build())
  .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_NO_ISSUE + " INC3").build())
  .build()
  .run()
  .hasTrimmedFlatOutput(
    "refs/heads/master e2bc4ed003 -> af35d5c1a4  2 Tomas <my@email.com> >>> SB-5678 fixing stuff INC3 Dont include INC * INC   Incident, INC  3 Tomas <my@email.com> >>> fixing stuff INC3 You need to specity JIRA * ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)   JIRA Dont include INC * INC   Incident, INC")
    .wasRejected();
 }
}
