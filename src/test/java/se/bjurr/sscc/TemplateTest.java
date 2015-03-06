package se.bjurr.sscc;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_ACCEPT_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_BRANCHES;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_DRY_RUN;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_DRY_RUN_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_ACCEPT;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_MATCH;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REJECT_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_RULE_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_RULE_REGEXP;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

import se.bjurr.sscc.data.SSCCPerson;
import se.bjurr.sscc.settings.SSCCGroup;

import com.google.common.io.Resources;

public class TemplateTest {
 private static String RESPONSE_REJECT_TXT = "response_reject.txt";
 private static String RESPONSE_SUCCESS_TXT = "response_success.txt";

 /**
  * Other test cases test edge cases. This is intended to be used as an example
  * of how the entire response looks like. And again, dont test edge cases like
  * this, that will result in alot of work if the template is changed.
  */
 @Test
 public void testThatRejectResponseLooksGood() throws IOException {
  SSCCPerson committerFull = new SSCCPerson("Tommy Boy", "the@correct.email");
  SSCCPerson committerEmail = new SSCCPerson(null, "tom@pa.da");
  SSCCPerson committerName = new SSCCPerson("Tompa", null);
  SSCCPerson committerNone = new SSCCPerson(null, null);
  refChangeBuilder()
    .withHookNameVersion("Simple Stash Commit Checker X.X")
    .withStashEmail("the@correct.email")
    .withStashName("Tommy Boy")
    .withGroupAcceptingAtLeastOneJira()
    .withSetting(SETTING_BRANCHES, "master")
    .withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.ACCEPT.toString())
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ONE.toString())
    .withSetting(SETTING_GROUP_MESSAGE + "[0]",
      "Every commit needs at least one issue. If this is just refactoring or other code cleanup, you can use JIRA AB-1234.")
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)")
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA")
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC[0-9]*")
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident")
    .withSetting(SETTING_GROUP_ACCEPT + "[1]", SSCCGroup.Accept.ACCEPT.toString())
    .withSetting(SETTING_GROUP_MATCH + "[1]", SSCCGroup.Match.NONE.toString())
    .withSetting(SETTING_GROUP_MESSAGE + "[1]", "Dont mention review")
    .withSetting(SETTING_RULE_REGEXP + "[1][0]", ".*review.*")
    .withSetting(SETTING_RULE_MESSAGE + "[1][0]",
      "Dont mention that you are fixing a review, say what you are changing and why it needs to be changed!")
    .withSetting(SETTING_GROUP_ACCEPT + "[2]", SSCCGroup.Accept.SHOW_MESSAGE.toString())
    .withSetting(SETTING_GROUP_MATCH + "[2]", SSCCGroup.Match.NONE.toString())
    .withSetting(SETTING_GROUP_MESSAGE + "[2]",
      "It is easier to maintain the code if you create a JIRA issue for every incident. But this is optional for now.")
    .withSetting(SETTING_RULE_REGEXP + "[2][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE,
      "Please set correct email in your commits. git config --global user.email user@name.com")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, FALSE)
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE,
      "Please set correct name in commits. git config --global user.name \"User Name\"")
    .withSetting(SETTING_DRY_RUN, TRUE)
    .withSetting(
      SETTING_DRY_RUN_MESSAGE,
      "*** We are currently running commit checker in dry run mode. Your commits are\n"
        + "*** being accepted. We will soon start blocking this kind of commits.")
    .withSetting(SETTING_REJECT_MESSAGE, Resources.toString(getResource(RESPONSE_REJECT_TXT), UTF_8))
    .withSetting(SETTING_ACCEPT_MESSAGE, Resources.toString(getResource(RESPONSE_SUCCESS_TXT), UTF_8)) //
    .withChangeSet(changeSetBuilder() //
      .withId("10fe5ad13bbd9c180a4668334cda9c83cd92dd46") //
      .withMessage("Fixing review comments from previous commits") //
      .withCommitter(committerFull).build()) //
    .withChangeSet(changeSetBuilder() //
      .withId("26094a0739ed397b0d475f4a8d3af35f33a6a0cf") //
      .withMessage("fix") //
      .withCommitter(committerEmail).build()) //
    .withChangeSet(changeSetBuilder() //
      .withId("af35f33a6a26094a0739ed397b0d475f4a8d30cf") //
      .withMessage("SB-1234 Implementing feature....") //
      .withCommitter(committerName).build()) //
    .withChangeSet(changeSetBuilder() //
      .withId("97b0d475f4a8d326094a0739ed3af35f33a6a0cf") //
      .withMessage("INC123 Solving incident with....") //
      .withCommitter(committerNone).build()) //
    .build().run().hasOutputFrom("testProdThatRejectResponseLooksGood.txt").wasAccepted();
 }

 @Test
 public void testThatSuccessResponseIncludesAcceptMessageAndShowMessage() throws IOException {
  refChangeBuilder()
    .withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.SHOW_MESSAGE.toString())
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ONE.toString())
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for not specifying a Jira =)")
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)")
    .withSetting(SETTING_ACCEPT_MESSAGE, "Accepted by me")
    .withSetting(SETTING_REJECT_MESSAGE, "Rejected by me")
    .withChangeSet(changeSetBuilder().withId("1").withMessage("SB-5678 fixing stuff").build())
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "Accepted by me refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> SB-5678 fixing stuff  - Thanks for not specifying a Jira =)")
    .wasAccepted();
 }

 @Test
 public void testThatSuccessResponseIncludesAcceptMessageAndShowNegMessage() throws IOException {
  refChangeBuilder().withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.SHOW_MESSAGE.toString())
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.NONE.toString())
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for not specifying a Jira =)")
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)")
    .withSetting(SETTING_ACCEPT_MESSAGE, "Accepted by me").withSetting(SETTING_REJECT_MESSAGE, "Rejected by me")
    .withChangeSet(changeSetBuilder().withId("1").withMessage("SB-5678 fixing stuff").build()).build().run()
    .hasTrimmedFlatOutput("Accepted by me").wasAccepted();
 }

 /**
  * Other test cases test edge cases. This is intended to be used as an example
  * of how the entire response looks like. And again, dont test edge cases like
  * this, that will result in alot of work if the template is changed.
  */
 @Test
 public void testThatSuccessResponseLooksGood() throws IOException {
  refChangeBuilder().withSetting(SETTING_REJECT_MESSAGE, Resources.toString(getResource(RESPONSE_REJECT_TXT), UTF_8))
    .withSetting(SETTING_ACCEPT_MESSAGE, Resources.toString(getResource(RESPONSE_SUCCESS_TXT), UTF_8))
    .withChangeSet(changeSetBuilder().withId("1").withMessage("SB-5678 fixing stuff").build()).build().run()
    .hasOutputFrom("testProdThatSuccessResponseLooksGood.txt").wasAccepted();
 }
}
