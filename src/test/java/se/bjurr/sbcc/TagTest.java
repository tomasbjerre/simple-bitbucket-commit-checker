package se.bjurr.sbcc;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_JIRA_INC;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_BRANCH_REJECTION_REGEXP;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_EXCLUDE_TAG_COMMITS;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

import se.bjurr.sbcc.data.SbccPerson;

public class TagTest {
  @Test
  public void testThatBitbucketIgnoresBranchCheckForTagWhenTagsAreExcluded() throws IOException {
    refChangeBuilder() //
        .withBitbucketUserSlug("tomasbjerre")
        .withChangeSet(
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
                .build()) //
        .withSetting(SETTING_EXCLUDE_TAG_COMMITS, TRUE) //
        .withSetting(
            SETTING_BRANCH_REJECTION_REGEXP, "^refs/heads/(?:feature|bugfix|hotfix|release)(?:/).+")
        .withRefId("refs/tags/123") //
        .build() //
        .run() //
        .hasNoOutput() //
        .wasAccepted();
  }

  @Test
  public void testThatBitbucketIgnoresBranchCheckForTagWhenTagsAreNotExcluded() throws IOException {
    refChangeBuilder() //
        .withBitbucketUserSlug("tomasbjerre")
        .withChangeSet(
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
                .build()) //
        .withSetting(SETTING_EXCLUDE_TAG_COMMITS, FALSE) //
        .withSetting(
            SETTING_BRANCH_REJECTION_REGEXP, "^refs/heads/(?:feature|bugfix|hotfix|release)(?:/).+")
        .withRefId("refs/tags/123") //
        .build() //
        .run() //
        .hasNoOutput() //
        .wasAccepted();
  }

  @Test
  public void testThatBitbucketIgnoresCommitCheckForTagWhenTagsAreExcluded() throws IOException {
    refChangeBuilder() //
        .withBitbucketUserSlug("tomasbjerre")
        .withChangeSet(
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
                .withAuthor(new SbccPerson("Some Name", "emailAddress")) //
                .build()) //
        .withBitbucketDisplayName("Tomas Author")
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, TRUE)
        .withSetting(SETTING_EXCLUDE_TAG_COMMITS, TRUE) //
        .withRefId("refs/tags/123") //
        .build() //
        .run() //
        .hasNoOutput() //
        .wasAccepted();
  }

  @Test
  public void testThatBitbucketDoesNotIgnoresCommitCheckForTagWhenTagsAreNotExcluded()
      throws IOException {
    refChangeBuilder() //
        .withBitbucketUserSlug("tomasbjerre")
        .withChangeSet(
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
                .withAuthor(new SbccPerson("Some Name", "emailAddress")) //
                .build()) //
        .withBitbucketDisplayName("Tomas Author")
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, TRUE)
        .withSetting(SETTING_EXCLUDE_TAG_COMMITS, FALSE) //
        .withRefId("refs/tags/123") //
        .build() //
        .run() //
        .wasRejected();
  }

  @Test
  public void testThatBitbucketDoesNotIgnoreTagMessageCheckWhenTagsAreNotExcludedReject()
      throws IOException {
    refChangeBuilder() //
        .withGroupAcceptingOnlyBothJiraAndIncInEachCommit()
        .withBitbucketUserSlug("tomasbjerre")
        .withChangeSet(
            changeSetBuilder() //
                .withId("1") //
                .withMessage("") //
                .withAuthor(new SbccPerson("Some Name", "emailAddress")) //
                .build()) //
        .withSetting(SETTING_EXCLUDE_TAG_COMMITS, FALSE) //
        .withRefId("refs/tags/123") //
        .build() //
        .run() //
        .wasRejected() //
        .hasTrimmedFlatOutput(
            "refs/tags/123 e2bc4ed003 -> af35d5c1a4   1 Some Name <emailAddress> >>>   - You need to specity JIRA and INC   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)   Incident, INC: INC");
  }

  @Test
  public void
      testThatBitbucketDoesNotIgnoreTagMessageCheckWhenTagsAreNotExcludedRejectIgnoredIfDisabled()
          throws IOException {
    refChangeBuilder() //
        .withGroupAcceptingOnlyBothJiraAndIncInEachCommit()
        .withBitbucketUserSlug("tomasbjerre")
        .withChangeSet(
            changeSetBuilder() //
                .withId("1") //
                .withMessage("") //
                .withAuthor(new SbccPerson("Some Name", "emailAddress")) //
                .build()) //
        .withSetting(SETTING_EXCLUDE_TAG_COMMITS, TRUE) //
        .withRefId("refs/tags/123") //
        .build() //
        .run() //
        .wasAccepted();
  }

  @Test
  public void testThatBitbucketDoesNotIgnoreTagMessageCheckWhenTagsAreNotExcludedAccept()
      throws IOException {
    refChangeBuilder() //
        .withGroupAcceptingOnlyBothJiraAndIncInEachCommit()
        .withBitbucketUserSlug("tomasbjerre")
        .withChangeSet(
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_JIRA_INC) //
                .withAuthor(new SbccPerson("Some Name", "emailAddress")) //
                .build()) //
        .withSetting(SETTING_EXCLUDE_TAG_COMMITS, FALSE) //
        .withRefId("refs/tags/123") //
        .build() //
        .run() //
        .wasAccepted();
  }
}
