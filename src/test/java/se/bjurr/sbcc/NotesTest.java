package se.bjurr.sbcc;

import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_BRANCH_REJECTION_REGEXP;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class NotesTest {
  @Test
  public void testThatBitbucketIgnoresNotes() throws IOException {
    refChangeBuilder() //
        .withBitbucketUserSlug("tomasbjerre")
        .withChangeSet(
            changeSetBuilder() //
                .withId("1") //
                .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
                .build()) //
        .withSetting(
            SETTING_BRANCH_REJECTION_REGEXP, "^refs/heads/(?:feature|bugfix|hotfix|release)(?:/).+")
        .withRefId("refs/notes/release_notes") //
        .build() //
        .run() //
        .hasNoOutput() //
        .wasAccepted();
  }
}
