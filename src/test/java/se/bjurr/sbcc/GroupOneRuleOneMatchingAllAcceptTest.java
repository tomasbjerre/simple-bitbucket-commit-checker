package se.bjurr.sbcc;

import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_JIRA_INC;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class GroupOneRuleOneMatchingAllAcceptTest {

  @Test
  public void testThatItCanAcceptACommit() throws IOException {
    refChangeBuilder()
        .withGroupAcceptingOnlyBothJiraAndIncInEachCommit()
        .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA_INC).build())
        .build()
        .run()
        .hasNoOutput()
        .wasAccepted();
  }

  @Test
  public void testThatItCanAcceptMultipleCommits() throws IOException {
    refChangeBuilder()
        .withGroupAcceptingOnlyBothJiraAndIncInEachCommit()
        .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_JIRA_INC).build())
        .withChangeSet(
            changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA_INC + " hej").build())
        .build()
        .run()
        .hasNoOutput()
        .wasAccepted();
  }

  @Test
  public void testThatItCanRejectACommit() throws IOException {
    refChangeBuilder()
        .withGroupAcceptingOnlyBothJiraAndIncInEachCommit()
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
        .withGroupAcceptingOnlyBothJiraAndIncInEachCommit()
        .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build())
        .withChangeSet(changeSetBuilder().withId("2").withMessage(COMMIT_MESSAGE_JIRA_INC).build())
        .withChangeSet(
            changeSetBuilder().withId("3").withMessage(COMMIT_MESSAGE_JIRA_INC + "2").build())
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> fixing stuff  - You need to specity JIRA and INC   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)   Incident, INC: INC")
        .wasRejected();
  }
}
