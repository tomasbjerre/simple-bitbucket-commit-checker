package se.bjurr.sscc;

import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_BRANCHES;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class BranchesTest {
 @Test
 public void testThatBranchMatchingBranchSettingIsNotIgnored() throws IOException {
  refChangeBuilder() //
    .withOneAcceptGroup() //
    .withChangeSet(changeSetBuilder() //
      .withId("1") //
      .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
      .build()) //
    .withSetting(SETTING_BRANCHES, ".*master.*") //
    .withRefId("/refs/master").build().run().wasRejected();
 }

 @Test
 public void testThatEmptyBranchSettingIsNotIgnoringAnyBranches() throws IOException {
  refChangeBuilder() //
    .withOneAcceptGroup() //
    .withChangeSet(changeSetBuilder() //
      .withId("1") //
      .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
      .build()) //
    .withSetting(SETTING_BRANCHES, "") //
    .withRefId("/refs/master").build().run().wasRejected();
 }

 @Test
 public void testThatNoneBranchMatchingBranchSettingIsIgnored() throws IOException {
  refChangeBuilder() //
    .withOneAcceptGroup() //
    .withChangeSet(changeSetBuilder() //
      .withId("1") //
      .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
      .build()) //
    .withSetting(SETTING_BRANCHES, ".*master.*") //
    .withRefId("/refs/feature_x").build().run().wasAccepted();
 }
}
