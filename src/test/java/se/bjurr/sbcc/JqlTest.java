package se.bjurr.sbcc;

import static java.lang.Boolean.TRUE;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.data.SbccPersonBuilder.sbccPersonBuilder;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_COMMIT_REGEXP;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_JQL_CHECK;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_JQL_CHECK_MESSAGE;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_JQL_CHECK_QUERY;
import static se.bjurr.sbcc.util.RefChangeBuilder.JIRA_REGEXP;
import static se.bjurr.sbcc.util.RefChangeBuilder.JIRA_RESPONSE_EMPTY;
import static se.bjurr.sbcc.util.RefChangeBuilder.JIRA_RESPONSE_ONE;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import org.junit.Test;

public class JqlTest {

 private static final String JQL_STATUS_IN_PROGRESS = "status = \"In Progress\"";

 @Test
 public void testThatTheJQLQueryCanBeUsedWithoutVariablesToReject() throws Exception {
  refChangeBuilder().fakeJiraResponse(JQL_STATUS_IN_PROGRESS, JIRA_RESPONSE_EMPTY)
    .withChangeSet(changeSetBuilder().withId("1").withMessage("fixing stuff").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_JQL_CHECK_QUERY, JQL_STATUS_IN_PROGRESS)
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Must be in progess!").build().run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> fixing stuff  - JQL: status = \"In Progress\"   Must be in progess!")
    .wasRejected();
 }

 @Test
 public void testThatTheJQLQueryCanBeUsedWithoutVariablesToAccept() throws Exception {
  refChangeBuilder().fakeJiraResponse(JQL_STATUS_IN_PROGRESS, JIRA_RESPONSE_ONE)
    .withChangeSet(changeSetBuilder().withId("1").withMessage("fixing stuff").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_JQL_CHECK_QUERY, JQL_STATUS_IN_PROGRESS)
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Must be in progess!").build().run().hasNoOutput().wasAccepted();
 }

 @Test
 public void testThatTheJQLQueryCanBeUsedWithoutVariablesToAcceptTwoResults() throws Exception {
  refChangeBuilder().fakeJiraResponse(JQL_STATUS_IN_PROGRESS, JIRA_RESPONSE_ONE)
    .withChangeSet(changeSetBuilder().withId("1").withMessage("fixing stuff").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_JQL_CHECK_QUERY, JQL_STATUS_IN_PROGRESS)
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Must be in progess!").build().run().hasNoOutput().wasAccepted();
 }

 @Test
 public void testThatTheJQLQueryCanBeUsedWithUserVariable() throws Exception {
  refChangeBuilder().fakeJiraResponse("assignee in (\"tomas\")", JIRA_RESPONSE_ONE).withBitbucketName("tomas")
    .withChangeSet(changeSetBuilder().withId("1").withMessage("fixing stuff").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_JQL_CHECK_QUERY, "assignee in (\"${BITBUCKET_USER}\")")
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Must have assignee!").build().run().hasNoOutput().wasAccepted();
 }

 @Test
 public void testThatTheJQLQueryCanBeUsedWithChangesetVariables() throws Exception {
  refChangeBuilder().fakeJiraResponse("assignee in (\"tomas\")", JIRA_RESPONSE_ONE).withBitbucketName("tomas")
    .withChangeSet(changeSetBuilder().withId("1").withMessage("fixing stuff")
      .withAuthor(sbccPersonBuilder().withName("Author name").withEmailAddress("Author Email").build())
      .withCommitter(sbccPersonBuilder().withName("Committer name").withEmailAddress("Committer Email").build())
      .build())
    .withSetting(SETTING_JQL_CHECK, TRUE)
    .withSetting(SETTING_JQL_CHECK_QUERY,
      "assignee in (\"${BITBUCKET_USER}\", \"${COMMITTER_NAME}\", \"${COMMITTER_EMAIL}\", \"${AUTHOR_NAME}\", \"${AUTHOR_EMAIL}\")")
    .withSetting(SETTING_JQL_CHECK_MESSAGE,
      "Msg... \"${COMMITTER_NAME} \"${COMMITTER_EMAIL}\", \"${AUTHOR_NAME}\", \"${AUTHOR_EMAIL}\"")
    .build().run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Author name <Author Email> >>> fixing stuff  - JQL: assignee in (\"tomas\", \"Committer name\", \"Committer Email\", \"Author name\", \"Author Email\")   Msg... \"Committer name \"Committer Email\", \"Author name\", \"Author Email\"")
    .wasRejected();
 }

 @Test
 public void testThatTheJQLQueryCanBeUsedWithRegexpVariable() throws Exception {
  refChangeBuilder().fakeJiraResponse("issue = AB-1234", JIRA_RESPONSE_ONE).withBitbucketName("tomas")
    .withChangeSet(changeSetBuilder().withId("1").withMessage("AB-1234 fixing stuff").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_COMMIT_REGEXP, JIRA_REGEXP)
    .withSetting(SETTING_JQL_CHECK_QUERY, "issue = ${REGEXP}")
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Issue must exist!").build().run().hasNoOutput().wasAccepted();
 }

 @Test
 public void testThatTheJQLQueryCanBeUsedWithUserAndRegexpVariable() throws Exception {
  refChangeBuilder().fakeJiraResponse("assignee in (\"tomas\") AND issue = AB-1234", JIRA_RESPONSE_ONE)
    .withBitbucketName("tomas")
    .withChangeSet(changeSetBuilder().withId("1").withMessage("AB-1234 fixing stuff").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_COMMIT_REGEXP, JIRA_REGEXP)
    .withSetting(SETTING_JQL_CHECK_QUERY, "assignee in (\"${BITBUCKET_USER}\") AND issue = ${REGEXP}")
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Must have assignee!").build().run().hasNoOutput().wasAccepted();
 }

 @Test
 public void testThatTheJQLQueryCanMatchJiraWithFirstRegexp() throws Exception {
  refChangeBuilder().fakeJiraResponse("issue = AB-1234", JIRA_RESPONSE_ONE)
    .fakeJiraResponse("issue = CD-5678", JIRA_RESPONSE_EMPTY)
    .withChangeSet(changeSetBuilder().withId("1").withMessage("AB-1234 fixing stuff CD-5678").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_COMMIT_REGEXP, JIRA_REGEXP)
    .withSetting(SETTING_JQL_CHECK_QUERY, "issue = ${REGEXP}")
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Issue must exist!").build().run().hasNoOutput().wasAccepted();
 }

 @Test
 public void testThatTheJQLQueryCanMatchJiraWithSecondRegexp() throws Exception {
  refChangeBuilder().fakeJiraResponse("issue = AB-1234", JIRA_RESPONSE_EMPTY)
    .fakeJiraResponse("issue = CD-5678", JIRA_RESPONSE_ONE)
    .withChangeSet(changeSetBuilder().withId("1").withMessage("AB-1234 fixing stuff CD-5678").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_COMMIT_REGEXP, JIRA_REGEXP)
    .withSetting(SETTING_JQL_CHECK_QUERY, "issue = ${REGEXP}")
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Issue must exist!").build().run().hasNoOutput().wasAccepted();
 }

 @Test
 public void testThatTheJQLQueryCanMatchJiraWithFirstRegexpSecondCrashes() throws Exception {
  refChangeBuilder().fakeJiraResponse("issue = AB-1234", JIRA_RESPONSE_ONE)
    .withChangeSet(changeSetBuilder().withId("1").withMessage("AB-1234 fixing stuff CD-5678").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_COMMIT_REGEXP, JIRA_REGEXP)
    .withSetting(SETTING_JQL_CHECK_QUERY, "issue = ${REGEXP}")
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Issue must exist!").build().run().hasNoOutput().wasAccepted();
 }

 @Test
 public void testThatTheJQLQueryCanMatchJiraWithSecondRegexpFirstCrashes() throws Exception {
  refChangeBuilder().fakeJiraResponse("issue = CD-5678", JIRA_RESPONSE_EMPTY)
    .withChangeSet(changeSetBuilder().withId("1").withMessage("AB-1234 fixing stuff CD-5678").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_COMMIT_REGEXP, JIRA_REGEXP)
    .withSetting(SETTING_JQL_CHECK_QUERY, "issue = ${REGEXP}")
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Issue must exist!").build().run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> AB-1234 fixing stuff CD-5678  - JQL: issue = AB-1234   Issue must exist!  - JQL: issue = CD-5678   Issue must exist!")
    .wasRejected();
 }

 @Test
 public void testThatTheJQLQueryCanFailWithTwoRegexpMatching() throws Exception {
  refChangeBuilder().fakeJiraResponse("issue = AB-1234", JIRA_RESPONSE_EMPTY)
    .fakeJiraResponse("issue = CD-5678", JIRA_RESPONSE_EMPTY)
    .withChangeSet(changeSetBuilder().withId("1").withMessage("AB-1234 fixing stuff CD-5678").build())
    .withSetting(SETTING_JQL_CHECK, TRUE).withSetting(SETTING_COMMIT_REGEXP, JIRA_REGEXP)
    .withSetting(SETTING_JQL_CHECK_QUERY, "issue = ${REGEXP}")
    .withSetting(SETTING_JQL_CHECK_MESSAGE, "Issue must exist!").build().run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> AB-1234 fixing stuff CD-5678  - JQL: issue = AB-1234   Issue must exist!  - JQL: issue = CD-5678   Issue must exist!")
    .wasRejected();
 }
}
