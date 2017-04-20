package se.bjurr.sbcc;

import static java.lang.Boolean.TRUE;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.DEFAULT_COMMITTER;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME_BITBUCKET;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME_SLUG;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_COMMITTER_NAME;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_COMMITTER_NAME_SLUG;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

import se.bjurr.sbcc.data.SbccPerson;

public class MatchingNameTest {
  @Test
  public void
      testCommitAuthorIsAcceptedWhenNameMustMatchNameInBitbucketAndTwoOfTwoCommitsHaveDifferentEmail()
          throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("Tomas Committer 1", "committer@one.site"))
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withChangeSet(
            changeSetBuilder()
                .withId("2")
                .withCommitter(new SbccPerson("Tomas Committer 2", "tomas.bjerre@two.site"))
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("committer@one.site")
        .withBitbucketDisplayName("Tomas Author")
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Bitbucket not same as in commit")
        .build()
        .run()
        .hasNoOutput()
        .wasAccepted();
  }

  @Test
  public void testCommitAuthorMustMatchNameInBitbucketAndOnlyOneOfTwoCommitsIsOk()
      throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))
                .withAuthor(new SbccPerson("Tomas Diff", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withChangeSet(
            changeSetBuilder()
                .withId("2")
                .withCommitter(new SbccPerson("Tomas Committer", "tomas.bjerre@two.site"))
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("committer@one.site")
        .withBitbucketDisplayName("Tomas Author")
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Bitbucket not same as in commit")
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Diff <author@one.site> >>> SB-5678 fixing stuff  - Bitbucket: 'Tomas Author' != Commit: 'Tomas Diff'   Name in Bitbucket not same as in commit")
        .wasRejected();
  }

  @Test
  public void testCommitAuthorMustMatchNameInBitbucketAndOnlyOneOfTwoCommitsIsOkReverseOrder()
      throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withChangeSet(
            changeSetBuilder()
                .withId("2")
                .withCommitter(new SbccPerson("Tomas Committer", "tomas.bjerre@two.site"))
                .withAuthor(new SbccPerson("Tomas Diff", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("committer@one.site")
        .withBitbucketDisplayName("Tomas Author")
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Bitbucket not same as in commit")
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas Diff <author@one.site> >>> SB-5678 fixing stuff  - Bitbucket: 'Tomas Author' != Commit: 'Tomas Diff'   Name in Bitbucket not same as in commit")
        .wasRejected();
  }

  @Test
  public void
      testCommitIsAcceptedWhenNameMustMatchNameInBitbucketAndTwoOfTwoCommitsHaveDifferentEmail()
          throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withChangeSet(
            changeSetBuilder()
                .withId("2")
                .withCommitter(new SbccPerson("Tomas Committer", "tomas.bjerre@two.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("committer@one.site")
        .withBitbucketDisplayName("Tomas Committer")
        .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_NAME, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Bitbucket not same as in commit")
        .build()
        .run()
        .hasNoOutput()
        .wasAccepted();
  }

  @Test
  public void testCommitIsAcceptedWhenNameMustMatchSlugInBitbucket() throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("tomas", "committer@one.site"))
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("committer@one.site")
        .withBitbucketDisplayName("Tomas Committer")
        .withBitbucketUserSlug("tomas")
        .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_NAME_SLUG, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Slug in Bitbucket not same as in commit")
        .build()
        .run()
        .hasNoOutput()
        .wasAccepted();
  }

  @Test
  public void testCommitIsRejectedWhenNameMustMatchSlugInBitbucket() throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("tomas", "committer@one.site"))
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("committer@one.site")
        .withBitbucketDisplayName("Tomas Committer")
        .withBitbucketUserSlug("tomassso")
        .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_NAME_SLUG, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Slug in Bitbucket not same as in commit")
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Bitbucket: 'tomassso' != Commit: 'tomas'   Slug in Bitbucket not same as in commit")
        .wasRejected();
  }

  @Test
  public void testCommitIsAcceptedWhenAuthorNameMustMatchSlugInBitbucket() throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("tomas committer", "committer@one.site"))
                .withAuthor(new SbccPerson("tomas", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("committer@one.site")
        .withBitbucketDisplayName("Tomas Committer")
        .withBitbucketUserSlug("tomas")
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_SLUG, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Slug in Bitbucket not same as in commit")
        .build()
        .run()
        .hasNoOutput()
        .wasAccepted();
  }

  @Test
  public void testCommitIsRejectedWhenAuthorNameMustMatchSlugInBitbucket() throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("tomas committer", "committer@one.site"))
                .withAuthor(new SbccPerson("tomas", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("committer@one.site")
        .withBitbucketDisplayName("Tomas Committer")
        .withBitbucketUserSlug("tomassso")
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_SLUG, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Slug in Bitbucket not same as in commit")
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 tomas <author@one.site> >>> SB-5678 fixing stuff  - Bitbucket: 'tomassso' != Commit: 'tomas'   Slug in Bitbucket not same as in commit")
        .wasRejected();
  }

  @Test
  public void testCommitMustMatchNameInBitbucketAndOnlyOneOfTwoCommitsIsOk() throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withChangeSet(
            changeSetBuilder()
                .withId("2")
                .withCommitter(new SbccPerson("Tompa B", "tomas.bjerre@two.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("committer@one.site")
        .withBitbucketDisplayName("Tompa Committer")
        .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_NAME, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Bitbucket not same as in commit")
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Bitbucket: 'Tompa Committer' != Commit: 'Tomas Committer'   Name in Bitbucket not same as in commit   2 Tomas <my@email.com> >>> SB-5678 fixing stuff  - Bitbucket: 'Tompa Committer' != Commit: 'Tompa B'   Name in Bitbucket not same as in commit")
        .wasRejected();
  }

  @Test
  public void testCommitMustMatchNameInBitbucketAndOnlyOneOfTwoCommitsIsOkReverseOrder()
      throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("Tomas B", "committer@one.site"))
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withChangeSet(
            changeSetBuilder()
                .withId("2")
                .withCommitter(new SbccPerson("Tomas Committer", "tomas.bjerre@two.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("committer@one.site")
        .withBitbucketDisplayName("Tompa Committer")
        .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_NAME, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Bitbucket not same as in commit")
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Bitbucket: 'Tompa Committer' != Commit: 'Tomas B'   Name in Bitbucket not same as in commit   2 Tomas <my@email.com> >>> SB-5678 fixing stuff  - Bitbucket: 'Tompa Committer' != Commit: 'Tomas Committer'   Name in Bitbucket not same as in commit")
        .wasRejected();
  }

  @Test
  public void testThatAuthorNameCanBeAcceptedIfInBitbucket() throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(DEFAULT_COMMITTER)
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_BITBUCKET, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name not available in Bitbucket")
        .withUserInBitbucket("Tomas Author", "user.name", "user@email") //
        .build() //
        .run() //
        .hasNoOutput() //
        .wasAccepted();
  }

  @Test
  public void testThatAuthorNameCanBeRejectedIfNotInBitbucket() throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(DEFAULT_COMMITTER)
                .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_BITBUCKET, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name not available in Bitbucket")
        .withUserInBitbucket("Display Name", "user.name", "user@email")
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Commit: 'Tomas Author'   Name not available in Bitbucket")
        .wasRejected();
  }
}
