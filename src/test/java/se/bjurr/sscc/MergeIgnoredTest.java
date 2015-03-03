package se.bjurr.sscc;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_EXCLUDE_MERGE_COMMITS;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class MergeIgnoredTest {
 @Test
 public void testThatCommitIsAcceptedWhenMergeIgnored() throws IOException {
  refChangeBuilder().withAllAcceptJIRANoneAcceptINC().withSetting(SETTING_EXCLUDE_MERGE_COMMITS, TRUE)
  .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).withParentCount(2).build())
  .build().run().hasTrimmedFlatOutput("").wasAccepted();
 }

 @Test
 public void testThatCommitIsRejectedWhenMergeNotIgnored() throws IOException {
  refChangeBuilder()
  .withAllAcceptJIRANoneAcceptINC()
  .withSetting(SETTING_EXCLUDE_MERGE_COMMITS, FALSE)
  .withChangeSet(changeSetBuilder().withId("1").withMessage(COMMIT_MESSAGE_NO_ISSUE).withParentCount(2).build())
  .build()
  .run()
  .hasTrimmedFlatOutput(
    "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> fixing stuff  - You need to specity JIRA   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)")
    .wasRejected();
 }
}
