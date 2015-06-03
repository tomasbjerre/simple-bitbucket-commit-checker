package se.bjurr.sscc;

import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_COMMITTER_NAME;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

import se.bjurr.sscc.data.SSCCPerson;

public class MatchingNameTest {
 @Test
 public void testCommitMustMatchNameInStashAndOnlyOneOfTwoCommitsIsOk() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tompa B", "tomas.bjerre@two.site"))
        .withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("committer@one.site")
    .withStashDisplayName("Tompa Committer")
    .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_NAME, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Stash not same as in commit")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Stash: 'Tompa Committer' != Commit: 'Tomas Committer'   Name in Stash not same as in commit   2 Tomas <my@email.com> >>> SB-5678 fixing stuff  - Stash: 'Tompa Committer' != Commit: 'Tompa B'   Name in Stash not same as in commit")
    .wasRejected();
 }

 @Test
 public void testCommitMustMatchNameInStashAndOnlyOneOfTwoCommitsIsOkReverseOrder() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas B", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer", "tomas.bjerre@two.site"))
        .withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("committer@one.site")
    .withStashDisplayName("Tompa Committer")
    .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_NAME, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Stash not same as in commit")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Stash: 'Tompa Committer' != Commit: 'Tomas B'   Name in Stash not same as in commit   2 Tomas <my@email.com> >>> SB-5678 fixing stuff  - Stash: 'Tompa Committer' != Commit: 'Tomas Committer'   Name in Stash not same as in commit")
    .wasRejected();
 }

 @Test
 public void testCommitIsAcceptedWhenNameMustMatchNameInStashAndTwoOfTwoCommitsHaveDifferentEmail() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer", "tomas.bjerre@two.site"))
        .withMessage(COMMIT_MESSAGE_JIRA).build()).withStashEmail("committer@one.site")
    .withStashDisplayName("Tomas Committer").withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_NAME, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Stash not same as in commit").build().run()
    .hasNoOutput().wasAccepted();
 }

 @Test
 public void testCommitAuthorMustMatchNameInStashAndOnlyOneOfTwoCommitsIsOk() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Diff", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer", "tomas.bjerre@two.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("committer@one.site")
    .withStashDisplayName("Tomas Author")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Stash not same as in commit")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Diff <author@one.site> >>> SB-5678 fixing stuff  - Stash: 'Tomas Author' != Commit: 'Tomas Diff'   Name in Stash not same as in commit")
    .wasRejected();
 }

 @Test
 public void testCommitAuthorMustMatchNameInStashAndOnlyOneOfTwoCommitsIsOkReverseOrder() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer", "tomas.bjerre@two.site"))
        .withAuthor(new SSCCPerson("Tomas Diff", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("committer@one.site")
    .withStashDisplayName("Tomas Author")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Stash not same as in commit")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas Diff <author@one.site> >>> SB-5678 fixing stuff  - Stash: 'Tomas Author' != Commit: 'Tomas Diff'   Name in Stash not same as in commit")
    .wasRejected();
 }

 @Test
 public void testCommitAuthorIsAcceptedWhenNameMustMatchNameInStashAndTwoOfTwoCommitsHaveDifferentEmail()
   throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer 1", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer 2", "tomas.bjerre@two.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("committer@one.site").withStashDisplayName("Tomas Author")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE, "Name in Stash not same as in commit").build().run()
    .hasNoOutput().wasAccepted();
 }
}
