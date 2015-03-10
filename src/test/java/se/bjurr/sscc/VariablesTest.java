package se.bjurr.sscc;

import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

import se.bjurr.sscc.data.SSCCPerson;

public class VariablesTest {

 @Test
 public void testStashEmailVariableCanBeUsedInEmailRejectionMessage() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Commit Name", "commit@mail"))
        .withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("stash@mail")
    .withStashDisplayName("Stash Name")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
    .withSetting(
      SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE,
      "Stash says your email is ${" + SSCCRenderer.SSCCVariable.STASH_EMAIL
        + "}, set it using: git config --global user.email ${" + SSCCRenderer.SSCCVariable.STASH_EMAIL + "}")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Commit Name <commit@mail> >>> SB-5678 fixing stuff  - Stash: 'stash@mail' != Commit: 'commit@mail'   Stash says your email is stash@mail, set it using: git config --global user.email stash@mail")
    .wasRejected();
 }

 @Test
 public void testStashNameVariableCanBeUsedInNameRejectionMessage() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withCommitter(new SSCCPerson("Commit Name", "commit@mail"))
        .withMessage(COMMIT_MESSAGE_JIRA).build())
    .withStashEmail("stash@mail")
    .withStashDisplayName("Stash Name")
    .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, TRUE)
    .withSetting(
      SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE,
      "Stash says your name is ${" + SSCCRenderer.SSCCVariable.STASH_NAME
        + "}, set it using: git config --global user.name \"${" + SSCCRenderer.SSCCVariable.STASH_NAME + "}\"")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Commit Name <commit@mail> >>> SB-5678 fixing stuff  - Stash: 'Stash Name' != Commit: 'Commit Name'   Stash says your name is Stash Name, set it using: git config --global user.name \"Stash Name\"")
    .wasRejected();
 }
}
