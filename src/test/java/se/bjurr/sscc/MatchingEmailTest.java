package se.bjurr.sscc;

import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_REGEXP;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

import se.bjurr.sscc.data.SSCCPerson;

public class MatchingEmailTest {
 @Test
 public void testCommitMustMatchNameInStashAndOnlyOneOfTwoCommitsIsOk() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tompa B", "committer@diff.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("committer@one.site")
    .withStashDisplayName("Tompa Committer")
    .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Name in Stash not same as in commit")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Stash: 'committer@one.site' != Commit: 'committer@diff.site'   Name in Stash not same as in commit")
    .wasRejected();
 }

 @Test
 public void testCommitMustMatchNameInStashAndOnlyOneOfTwoCommitsIsOkReverseOrder() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "tomas.bjerre@two.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("committer@one.site")
    .withStashDisplayName("Tompa Committer")
    .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Name in Stash not same as in commit")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Stash: 'committer@one.site' != Commit: 'tomas.bjerre@two.site'   Name in Stash not same as in commit")
    .wasRejected();
 }

 @Test
 public void testCommitIsAcceptedWhenNameMustMatchNameInStashAndTwoOfTwoCommitsHaveDifferentEmail() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withMessage(COMMIT_MESSAGE_JIRA).build()).withStashEmail("committer@one.site")
    .withStashDisplayName("Tomas Committer").withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Name in Stash not same as in commit").build().run()
    .hasNoOutput().wasAccepted();
 }

 @Test
 public void testCommitAuthorMustMatchNameInStashAndOnlyOneOfTwoCommitsIsOk() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer", "tomas.bjerre@two.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@diff.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("author@one.site")
    .withStashDisplayName("Tompa Committer")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Name in Stash not same as in commit")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas Author <author@diff.site> >>> SB-5678 fixing stuff  - Stash: 'author@one.site' != Commit: 'author@diff.site'   Name in Stash not same as in commit")
    .wasRejected();
 }

 @Test
 public void testCommitAuthorMustMatchNameInStashAndOnlyOneOfTwoCommitsIsOkReverseOrder() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@diff.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer", "tomas.bjerre@two.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("author@one.site")
    .withStashDisplayName("Tompa Committer")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Name in Stash not same as in commit")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@diff.site> >>> SB-5678 fixing stuff  - Stash: 'author@one.site' != Commit: 'author@diff.site'   Name in Stash not same as in commit")
    .wasRejected();
 }

 @Test
 public void testCommitAuthorIsAcceptedWhenNameMustMatchNameInStashAndTwoOfTwoCommitsHaveDifferentEmail()
   throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer 1", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author 1", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer 2", "tomas.bjerre@two.site"))
        .withAuthor(new SSCCPerson("Tomas Author 2", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("author@one.site").withStashDisplayName("Tomas Author")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Name in Stash not same as in commit").build().run()
    .hasNoOutput().wasAccepted();
 }

 @Test
 public void testThatAuthorEmailCanBeRejectedByRegexp() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("author@one.site")
    .withStashDisplayName("Tompa Committer")
    .withStashName("regexp")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_REGEXP, "^${STASH_USER}@.*")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "not ok")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Stash: '^regexp@.*' != Commit: 'author@one.site'   not ok")
    .wasRejected();
 }

 @Test
 public void testThatCommitterEmailCanBeRejectedByRegexp() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("comitter@one.site")
    .withStashDisplayName("Tompa Committer")
    .withStashName("regexp")
    .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_REGEXP, "^${STASH_USER}@.*")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "not ok")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Stash: '^regexp@.*' != Commit: 'committer@one.site'   not ok")
    .wasRejected();
 }

 @Test
 public void testThatCommitterEmailCanBeRejectedByRegexpWithoutVariables() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@company.domain"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer", "committer@othercompany.domain"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("3").withCommitter(new SSCCPerson("Tomas Committer", "other.committer@company.domain"))
        .withAuthor(new SSCCPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("stash@email.site")
    .withStashDisplayName("Tompa Committer")
    .withStashName("regexp")
    .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_REGEXP, "^[^@]*@company.domain$")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "not ok")
    .build()
    .run()
    .wasRejected()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Stash: '^[^@]*@company.domain$' != Commit: 'committer@othercompany.domain'   not ok");
 }

 @Test
 public void testThatAuthorEmailCanBeRejectedByRegexpWithoutVariables() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "committer@company.domain")).withMessage(COMMIT_MESSAGE_JIRA)
        .build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "committer@othercompany.domain")).withMessage(COMMIT_MESSAGE_JIRA)
        .build())
    .withChangeSet(
      changeSetBuilder().withId("3").withCommitter(new SSCCPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SSCCPerson("Tomas Author", "other.committer@company.domain")).withMessage(COMMIT_MESSAGE_JIRA)
        .build())
    .withStashEmail("stash@email.site")
    .withStashDisplayName("Tompa Committer")
    .withStashName("regexp")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_REGEXP, "^[^@]*@company.domain$")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "not ok")
    .build()
    .run()
    .wasRejected()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas Author <committer@othercompany.domain> >>> SB-5678 fixing stuff  - Stash: '^[^@]*@company.domain$' != Commit: 'committer@othercompany.domain'   not ok");
 }
}
