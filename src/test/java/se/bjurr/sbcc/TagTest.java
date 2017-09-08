package se.bjurr.sbcc;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
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
}
