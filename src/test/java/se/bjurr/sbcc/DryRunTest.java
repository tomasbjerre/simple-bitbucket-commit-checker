package se.bjurr.sbcc;

import static java.lang.Boolean.TRUE;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_DRY_RUN;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_DRY_RUN_MESSAGE;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;
import org.junit.Test;

public class DryRunTest {

  @Test
  public void testThatDryRunAcceptsFaultyCommits() throws IOException {
    refChangeBuilder()
        .withSetting(SETTING_DRY_RUN, TRUE)
        .withGroupAcceptingAtLeastOneJira()
        .withChangeSet( //
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
                .build() //
            )
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> fixing stuff  - You need to specity an issue   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)")
        .wasAccepted();
  }

  @Test
  public void testThatDryRunAcceptsFaultyCommitsMessage() throws IOException {
    refChangeBuilder()
        .withSetting(SETTING_DRY_RUN, TRUE)
        .withSetting(SETTING_DRY_RUN_MESSAGE, "In dry run mode!")
        .withGroupAcceptingAtLeastOneJira()
        .withChangeSet( //
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
                .build() //
            )
        .build()
        .run()
        .hasTrimmedFlatOutput(
            "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>> fixing stuff  - You need to specity an issue   JIRA: ((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)  In dry run mode!")
        .wasAccepted();
  }

  @Test
  public void testThatDryRunCanProvideMessageFromSettings() throws IOException {
    refChangeBuilder() //
        .withSetting(SETTING_DRY_RUN, TRUE) //
        .withSetting(SETTING_DRY_RUN_MESSAGE, "In dry run mode!") //
        .withGroupAcceptingAtLeastOneJira() //
        .withChangeSet( //
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
                .build() //
            ) //
        .build() //
        .run() //
        .hasOutputFrom("testThatDryRunCanProvideMessageFromSettings.txt") //
        .wasAccepted();
  }
}
