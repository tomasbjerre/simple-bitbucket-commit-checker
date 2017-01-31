package se.bjurr.sbcc;

import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class GroupOneRuleOneMatchingOneShowMessageTest {

  @Test
  public void testThatItCanAcceptACommit() throws IOException {
    refChangeBuilder()
        .withGroupShowingMessageForAnyCommitContainingAtLeastOneJira()
        .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build())
        .build()
        .run()
        .hasNoOutput()
        .wasAccepted();
  }

  @Test
  public void testThatItCanAcceptMultipleCommits() throws IOException {
    refChangeBuilder()
        .withGroupShowingMessageForAnyCommitContainingAtLeastOneJira()
        .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build())
        .withChangeSet(
            changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_NO_ISSUE + " 2").build())
        .build()
        .run()
        .hasNoOutput()
        .wasAccepted();
  }

  @Test
  public void testThatItCanRejectACommit() throws IOException {
    refChangeBuilder()
        .withGroupShowingMessageForAnyCommitContainingAtLeastOneJira()
        .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA).build())
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> SB-5678 fixing stuff  - Thanks for specifying a Jira =)")
        .wasAccepted();
  }

  @Test
  public void testThatItCanRejectSomeOfMultipleCommits() throws IOException {
    refChangeBuilder()
        .withGroupShowingMessageForAnyCommitContainingAtLeastOneJira()
        .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA).build())
        .withChangeSet(
            changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_NO_ISSUE + " 2").build())
        .withChangeSet(
            changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_JIRA + " 3").build())
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> SB-5678 fixing stuff  - Thanks for specifying a Jira =)   3 Tomas <my@email.com> >>> SB-5678 fixing stuff 3  - Thanks for specifying a Jira =)")
        .wasAccepted();
  }
}
