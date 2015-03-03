package se.bjurr.sscc;

import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class GroupOneRuleOneMatchingAllAcceptTest {

 @Test
 public void testThatItCanAcceptACommit() throws IOException {
  refChangeBuilder().withAllAcceptGroup()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA + " INC143").build()).build().run()
    .hasTrimmedFlatOutput("").wasAccepted();
 }

 @Test
 public void testThatItCanAcceptMultipleCommits() throws IOException {
  refChangeBuilder().withAllAcceptGroup()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA + " INC123").build())
    .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA + " INC123 hej").build()).build()
    .run().hasTrimmedFlatOutput("").wasAccepted();
 }

 @Test
 public void testThatItCanRejectACommit() throws IOException {
  refChangeBuilder()
    .withAllAcceptGroup()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA).build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> SB-5678 fixing stuff  - You need to specity JIRA and INC   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)   Incident, INC: INC")
    .wasRejected();
 }

 @Test
 public void testThatItCanRejectSomeOfMultipleCommits() throws IOException {
  refChangeBuilder()
    .withAllAcceptGroup()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build())
    .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA + " INC2").build())
    .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_NO_ISSUE + " INC3").build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> fixing stuff  - You need to specity JIRA and INC   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)   Incident, INC: INC   3 Tomas <my@email.com> >>> fixing stuff INC3  - You need to specity JIRA and INC   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)   Incident, INC: INC")
    .wasRejected();
 }
}
