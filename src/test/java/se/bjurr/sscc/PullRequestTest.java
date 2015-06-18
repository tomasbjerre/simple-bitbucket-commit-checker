package se.bjurr.sscc;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sscc.SsccRepositoryMergeRequestCheck.PR_REJECT_DEFAULT_MSG;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_CHECK_PULLREQUESTS;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_CHECK_PULLREQUESTS_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_DRY_RUN;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class PullRequestTest {

 @Test
 public void testThatFaultyCommitsAreAcceptedInDryRunMode() throws IOException {
  refChangeBuilder().withSetting(SETTING_CHECK_PULLREQUESTS, TRUE).withSetting(SETTING_DRY_RUN, TRUE)
    .withGroupAcceptingAtLeastOneJira()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build()).build()
    .runPullRequest().prWasAccepted();
 }

 @Test
 public void testThatFaultyCommitsAreRejected() throws IOException {
  refChangeBuilder()
    .withSetting(SETTING_CHECK_PULLREQUESTS, TRUE)
    .withSetting(SETTING_DRY_RUN, FALSE)
    .withGroupAcceptingAtLeastOneJira()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build())
    .build()
    .runPullRequest()
    .prWasRejected()
    .hasTrimmedPrPrintOut(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> fixing stuff  - You need to specity an issue   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)")
    .hasTrimmedPrSummary(PR_REJECT_DEFAULT_MSG);
 }

 @Test
 public void testThatFaultyCommitsAreAcceptedIfNotCheckingPullRequests() throws IOException {
  refChangeBuilder().withSetting(SETTING_CHECK_PULLREQUESTS, FALSE).withSetting(SETTING_DRY_RUN, FALSE)
    .withGroupAcceptingAtLeastOneJira()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build()).build()
    .runPullRequest().prWasAccepted();
 }

 @Test
 public void testThatFaultyCommitsCanBeRejectedWithCustomSummary() throws IOException {
  refChangeBuilder().withSetting(SETTING_CHECK_PULLREQUESTS, TRUE)
    .withSetting(SETTING_CHECK_PULLREQUESTS_MESSAGE, "custom summary").withGroupAcceptingAtLeastOneJira()
    .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).build()).build()
    .runPullRequest().prWasRejected().hasTrimmedPrSummary("custom summary");
 }
}
