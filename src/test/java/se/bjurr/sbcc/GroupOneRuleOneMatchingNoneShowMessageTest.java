package se.bjurr.sbcc;

import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class GroupOneRuleOneMatchingNoneShowMessageTest {

 @Test
 public void testThatItCanAcceptACommit() throws IOException {
  refChangeBuilder()
    .withGroupShowingMessageToAllCommitsNotContainingJiraOrInc()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> fixing stuff  - Thanks for not specifying a Jira or INC =)")
    .wasAccepted();
 }

 @Test
 public void testThatItCanAcceptMultipleCommits() throws IOException {
  refChangeBuilder()
    .withGroupShowingMessageToAllCommitsNotContainingJiraOrInc()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build())
    .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_NO_ISSUE + " 2").build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> fixing stuff  - Thanks for not specifying a Jira or INC =)   2 Tomas <my@email.com> >>> fixing stuff 2  - Thanks for not specifying a Jira or INC =)")
    .wasAccepted();
 }

 @Test
 public void testThatItCanRejectACommit() throws IOException {
  refChangeBuilder().withGroupShowingMessageToAllCommitsNotContainingJiraOrInc()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA).build()).build().run()
    .hasTrimmedFlatOutput("").wasAccepted();
 }

 @Test
 public void testThatItCanRejectSomeOfMultipleCommits() throws IOException {
  refChangeBuilder()
    .withGroupShowingMessageToAllCommitsNotContainingJiraOrInc()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_NO_ISSUE + " 2").build())
    .withChangeSet(changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_JIRA + " 3").build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas <my@email.com> >>> fixing stuff 2  - Thanks for not specifying a Jira or INC =)")
    .wasAccepted();
 }
}
