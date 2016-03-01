package se.bjurr.sbcc;

import static java.lang.Boolean.TRUE;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.DEFAULT_COMMITTER;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_BITBUCKET;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_REGEXP;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

import se.bjurr.sbcc.data.SbccPerson;

public class MatchingEmailTest {
 @Test
 public void testCommitMustMatchEmailInBitbucketAndOnlyOneOfTwoCommitsIsOk() throws IOException {
  refChangeBuilder()//
    .withChangeSet(//
      changeSetBuilder()//
        .withId("1")//
        .withCommitter(new SbccPerson("Tomas Committer", "Committer@one.site"))//
        .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))//
        .withMessage(COMMIT_MESSAGE_JIRA)//
        .build())//
    .withChangeSet(//
      changeSetBuilder()//
        .withId("2")//
        .withCommitter(new SbccPerson("Tompa B", "commiTter@diff.site"))//
        .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))//
        .withMessage(COMMIT_MESSAGE_JIRA)//
        .build()//
    )//
    .withBitbucketEmail("committer@one.site")//
    .withBitbucketDisplayName("Tompa Committer")//
    .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL, TRUE)//
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Name in Bitbucket not same as in commit")//
    .build()//
    .run()//
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Bitbucket: 'committer@one.site' != Commit: 'commiTter@diff.site'   Name in Bitbucket not same as in commit")//
    .wasRejected();
 }

 @Test
 public void testCommitMustMatchEmailInBitbucketAndOnlyOneOfTwoCommitsIsOkReverseOrder() throws IOException {
  refChangeBuilder()//
    .withChangeSet(//
      changeSetBuilder()//
        .withId("1")//
        .withCommitter(new SbccPerson("Tomas Committer", "tomas.bjerre@two.site"))//
        .withAuthor(new SbccPerson("Tomas Author", "author@one.site"))//
        .withMessage(COMMIT_MESSAGE_JIRA)//
        .build()//
    )//
    .withChangeSet(//
      changeSetBuilder()//
        .withId("2")//
        .withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))//
        .withMessage(COMMIT_MESSAGE_JIRA)//
        .build()//
    )//
    .withBitbucketEmail("committer@one.site")//
    .withBitbucketDisplayName("Tompa Committer")//
    .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL, TRUE)//
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Name in Bitbucket not same as in commit")//
    .build()//
    .run()//
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Bitbucket: 'committer@one.site' != Commit: 'tomas.bjerre@two.site'   Name in Bitbucket not same as in commit")//
    .wasRejected();
 }

 @Test
 public void testCommitIsAcceptedWhenEmailMustMatchEmailInBitbucketAndTwoOfTwoCommitsHaveDifferentAuthor()
   throws IOException {
  refChangeBuilder()//
    .withChangeSet(//
      changeSetBuilder()//
        .withId("1")//
        .withCommitter(new SbccPerson("Tomas Committer", "Committer@one.site"))//
        .withAuthor(new SbccPerson("Tomas Author", "author@other.site"))//
        .withMessage(COMMIT_MESSAGE_JIRA)//
        .build()//
    )//
    .withChangeSet(//
      changeSetBuilder()//
        .withId("2")//
        .withCommitter(new SbccPerson("Tomas Committer", "cOmmitter@one.site"))//
        .withMessage(COMMIT_MESSAGE_JIRA)//
        .build()//
    )//
    .withBitbucketEmail("committer@one.site")//
    .withBitbucketDisplayName("Tomas Committer")//
    .withSetting(SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL, TRUE)//
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Email in Bitbucket not same as in commit")//
    .build()//
    .run()//
    .hasNoOutput()//
    .wasAccepted();
 }

 @Test
 public void testCommitAuthorMustMatchNameInBitbucketAndOnlyOneOfTwoCommitsIsOk() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SbccPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SbccPerson("Tomas Committer", "tomas.bjerre@two.site"))
        .withAuthor(new SbccPerson("Tomas Author", "author@diff.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withBitbucketEmail("author@one.site")
    .withBitbucketDisplayName("Tompa Committer")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Name in Bitbucket not same as in commit")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas Author <author@diff.site> >>> SB-5678 fixing stuff  - Bitbucket: 'author@one.site' != Commit: 'author@diff.site'   Name in Bitbucket not same as in commit")
    .wasRejected();
 }

 @Test
 public void testCommitAuthorMustMatchNameInBitbucketAndOnlyOneOfTwoCommitsIsOkReverseOrder() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SbccPerson("Tomas Author", "author@diff.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SbccPerson("Tomas Committer", "tomas.bjerre@two.site"))
        .withAuthor(new SbccPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withBitbucketEmail("author@one.site")
    .withBitbucketDisplayName("Tompa Committer")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Author email in Bitbucket not same as in commit")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@diff.site> >>> SB-5678 fixing stuff  - Bitbucket: 'author@one.site' != Commit: 'author@diff.site'   Author email in Bitbucket not same as in commit")
    .wasRejected();
 }

 @Test
 public void testThatAuthorEmailCanBeRejectedByRegexp() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SbccPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withBitbucketEmail("author@one.site")
    .withBitbucketDisplayName("Tompa Committer")
    .withBitbucketName("regexp")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_REGEXP, "^${BITBUCKET_USER}@.*")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "not ok")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Bitbucket: '^regexp@.*' != Commit: 'author@one.site'   not ok")
    .wasRejected();
 }

 @Test
 public void testThatAuthorEmailCanBeRejectedByRegexpWithoutVariables() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SbccPerson("Tomas Author", "committer@company.domain")).withMessage(COMMIT_MESSAGE_JIRA)
        .build())
    .withChangeSet(
      changeSetBuilder().withId("2").withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SbccPerson("Tomas Author", "committer@othercompany.domain")).withMessage(COMMIT_MESSAGE_JIRA)
        .build())
    .withChangeSet(
      changeSetBuilder().withId("3").withCommitter(new SbccPerson("Tomas Committer", "committer@one.site"))
        .withAuthor(new SbccPerson("Tomas Author", "other.committer@company.domain")).withMessage(COMMIT_MESSAGE_JIRA)
        .build())
    .withBitbucketEmail("bitbucket@email.site")
    .withBitbucketDisplayName("Tompa Committer")
    .withBitbucketName("regexp")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_REGEXP, "^[^@]*@company.domain$")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "not ok")
    .build()
    .run()
    .wasRejected()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   2 Tomas Author <committer@othercompany.domain> >>> SB-5678 fixing stuff  - Bitbucket: '^[^@]*@company.domain$' != Commit: 'committer@othercompany.domain'   not ok");
 }

 @Test
 public void testThatAuthorEmailCanBeRejectedIfNotInBitbucket() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(DEFAULT_COMMITTER)
        .withAuthor(new SbccPerson("Tomas Author", "author@one.site")).withMessage(COMMIT_MESSAGE_JIRA).build())
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_BITBUCKET, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Email not available in Bitbucket")
    .withUserInBitbucket("Display Name", "user.email", "user@email")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas Author <author@one.site> >>> SB-5678 fixing stuff  - Commit: 'author@one.site'   Email not available in Bitbucket")
    .wasRejected();
 }

 @Test
 public void testThatAuthorEmailCanBeAcceptedIfInBitbucket() throws IOException {
  refChangeBuilder()//
    .withChangeSet(//
      changeSetBuilder()//
        .withId("1")//
        .withCommitter(DEFAULT_COMMITTER)//
        .withAuthor(new SbccPerson("Tomas Author", "AuthoR@one.site"))//
        .withMessage(COMMIT_MESSAGE_JIRA)//
        .build()//
    )//
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_BITBUCKET, TRUE)//
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE, "Email not available in Bitbucket")//
    .withUserInBitbucket("Display Name", "author@one.site", "user@email")//
    .build()//
    .run()//
    .hasNoOutput()//
    .wasAccepted();
 }
}
