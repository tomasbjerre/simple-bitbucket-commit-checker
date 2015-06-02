package se.bjurr.sscc;

import static se.bjurr.sscc.SSCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_BRANCHES;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_BRANCH_REJECTION_REGEXP;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_BRANCH_REJECTION_REGEXP_MESSAGE;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class BranchesTest {
 @Test
 public void testThatEmptyIsNotIgnoringRefsMaster() throws IOException {
  refChangeBuilder() //
    .withGroupAcceptingAtLeastOneJira() //
    .withChangeSet(changeSetBuilder() //
      .withId("1") //
      .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
      .build()) //
    .withSetting(SETTING_BRANCHES, "") //
    .withRefId("/refs/master").build().run().wasRejected();
 }

 @Test
 public void testThatRegexpAnyMasterAnyIsIgnoredForRefsFeature() throws IOException {
  refChangeBuilder() //
    .withGroupAcceptingAtLeastOneJira() //
    .withChangeSet(changeSetBuilder() //
      .withId("1") //
      .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
      .build()) //
    .withSetting(SETTING_BRANCHES, ".*master.*") //
    .withRefId("/refs/feature").build().run().wasAccepted();
 }

 @Test
 public void testThatRegexpAnyMasterAnyIsNotIgnoredForRefsMaster() throws IOException {
  refChangeBuilder() //
    .withGroupAcceptingAtLeastOneJira() //
    .withChangeSet(changeSetBuilder() //
      .withId("1") //
      .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
      .build()) //
    .withSetting(SETTING_BRANCHES, ".*master.*") //
    .withRefId("/refs/master").build().run().wasRejected();
 }

 @Test
 public void testThatRegexpAnythingButRelAnyIsIgnoredForRefsRel() throws IOException {
  refChangeBuilder() //
    .withGroupAcceptingAtLeastOneJira() //
    .withChangeSet(changeSetBuilder() //
      .withId("1") //
      .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
      .build()) //
    .withSetting(SETTING_BRANCHES, "^/refs/((?!rel).).*") //
    .withRefId("/refs/rel_20150101").build().run().wasAccepted();
 }

 @Test
 public void testThatRegexpAnythingButRelAnyIsNotIgnoredForRefsDev() throws IOException {
  refChangeBuilder() //
    .withGroupAcceptingAtLeastOneJira() //
    .withChangeSet(changeSetBuilder() //
      .withId("1") //
      .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
      .build()) //
    .withSetting(SETTING_BRANCHES, "^/refs/((?!rel).).*") //
    .withRefId("/refs/dev_20150101").build().run().wasRejected();
 }

 @Test
 public void testThatBranchCanBeRejectedByRegexp() throws IOException {
  refChangeBuilder()
    .withRefId("/ref/feeture")
    .withChangeSet(changeSetBuilder() //
      .withId("1") //
      .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
      .build())
    .withSetting(SETTING_BRANCH_REJECTION_REGEXP, "/ref/(master|feature)$")
    .withSetting(SETTING_BRANCH_REJECTION_REGEXP_MESSAGE, "not ok")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "/ref/feeture e2bc4ed003 -> af35d5c1a4  - Branch: /ref/feeture, /ref/(master|feature)$   not ok").wasRejected();
 }

 @Test
 public void testThatBranchCanBeAcceptedByRegexp() throws IOException {
  refChangeBuilder() //
    .withRefId("/ref/feature") //
    .withChangeSet(changeSetBuilder() //
      .withId("1") //
      .withMessage(COMMIT_MESSAGE_NO_ISSUE) //
      .build()) //
    .withSetting(SETTING_BRANCH_REJECTION_REGEXP, "/ref/(master|feature)$") //
    .withSetting(SETTING_BRANCH_REJECTION_REGEXP_MESSAGE, "not ok") //
    .build().run().hasNoOutput().wasAccepted();
 }
}
