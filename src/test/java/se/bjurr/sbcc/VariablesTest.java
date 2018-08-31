package se.bjurr.sbcc;

import static java.lang.Boolean.TRUE;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_JIRA;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;
import org.junit.Test;
import se.bjurr.sbcc.data.SbccPerson;

public class VariablesTest {

  @Test
  public void testBitbucketAuthorEmailVariableCanBeUsedInEmailRejectionMessage()
      throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("Commit Name", "commit@mail"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .withAuthor(new SbccPerson("Commit Name", "author@mail"))
                .build())
        .withBitbucketEmail("bitbucket@mail")
        .withBitbucketDisplayName("Bitbucket Name")
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE,
            "Bitbucket says your email is ${"
                + SbccRenderer.SBCCVariable.BITBUCKET_EMAIL
                + "}, set it using: git config --global user.email ${"
                + SbccRenderer.SBCCVariable.BITBUCKET_EMAIL
                + "}")
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Commit Name <author@mail> >>> SB-5678 fixing stuff  - Bitbucket: 'bitbucket@mail' != Commit: 'author@mail'   Bitbucket says your email is bitbucket@mail, set it using: git config --global user.email bitbucket@mail")
        .wasRejected();
  }

  @Test
  public void testBitbucketAuthorNameVariableCanBeUsedInNameRejectionMessage() throws IOException {
    refChangeBuilder()
        .withChangeSet(
            changeSetBuilder()
                .withId("1")
                .withCommitter(new SbccPerson("Commit Name", "commit@mail"))
                .withMessage(COMMIT_MESSAGE_JIRA)
                .build())
        .withBitbucketEmail("bitbucket@mail")
        .withBitbucketDisplayName("Bitbucket Name")
        .withSetting(SETTING_REQUIRE_MATCHING_AUTHOR_NAME, TRUE)
        .withSetting(
            SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE,
            "Bitbucket says your name is ${"
                + SbccRenderer.SBCCVariable.BITBUCKET_NAME
                + "}, set it using: git config --global user.name \"${"
                + SbccRenderer.SBCCVariable.BITBUCKET_NAME
                + "}\"")
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> SB-5678 fixing stuff  - Bitbucket: 'Bitbucket Name' != Commit: 'Tomas'   Bitbucket says your name is Bitbucket Name, set it using: git config --global user.name \"Bitbucket Name\"")
        .wasRejected();
  }
}
