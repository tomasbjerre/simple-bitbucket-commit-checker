package se.bjurr.sscc;

import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class GroupOneRuleOneMatchingAllShowMessageTest {

 @Test
 public void testThatItCanAcceptACommit() throws IOException {
  refChangeBuilder().withAllShowMessageGroup()
  .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build()).build().run()
  .hasTrimmedFlatOutput("").wasAccepted();
 }

 @Test
 public void testThatItCanAcceptMultipleCommits() throws IOException {
  refChangeBuilder().withAllShowMessageGroup()
  .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build())
  .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_NO_ISSUE + " 2").build()).build().run()
  .hasTrimmedFlatOutput("").wasAccepted();
 }

 @Test
 public void testThatItCanRejectACommit() throws IOException {
  refChangeBuilder()
  .withAllShowMessageGroup()
  .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA + " INC123").build())
  .build()
  .run()
  .hasTrimmedFlatOutput(
    "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> SB-5678 fixing stuff INC123  - Thanks for specifying a Jira and INC =)")
    .wasAccepted();
 }

 @Test
 public void testThatItCanRejectSomeOfMultipleCommits() throws IOException {
  refChangeBuilder()
  .withAllShowMessageGroup()
  .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA).build())
  .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_NO_ISSUE + " 2").build())
  .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_JIRA + " INC123").build())
  .build()
  .run()
  .hasTrimmedFlatOutput(
    "refs/heads/master e2bc4ed003 -> af35d5c1a4   3 Tomas <my@email.com> >>> SB-5678 fixing stuff INC123  - Thanks for specifying a Jira and INC =)")
    .wasAccepted();
 }
}
