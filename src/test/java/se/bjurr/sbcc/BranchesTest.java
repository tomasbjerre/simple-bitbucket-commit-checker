package se.bjurr.sbcc;

import static com.atlassian.bitbucket.repository.RefChangeType.DELETE;
import static se.bjurr.sbcc.SBCCTestConstants.COMMIT_MESSAGE_NO_ISSUE;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_BRANCHES;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_BRANCH_REJECTION_REGEXP;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_BRANCH_REJECTION_REGEXP_MESSAGE;
import static se.bjurr.sbcc.util.RefChangeBuilder.refChangeBuilder;

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
    .withRefId("/refs/master") //
    .build() //
    .run() //
    .wasRejected();
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
    .withRefId("/refs/feature") //
    .build() //
    .run() //
    .wasAccepted();
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
    .withRefId("/refs/master") //
    .build() //
    .run() //
    .wasRejected();
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
    .withRefId("/refs/rel_20150101") //
    .build() //
    .run() //
    .wasAccepted();
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
    .withRefId("/refs/dev_20150101") //
    .build() //
    .run() //
    .wasRejected();
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
      "/ref/feeture e2bc4ed003 -> af35d5c1a4  - Branch: /ref/feeture, /ref/(master|feature)$   not ok") //
    .wasRejected();
 }

 @Test
 public void testThatBranchCanBeRejectedByRegexpEvenIfNoCommitsArePushed() throws IOException {
  refChangeBuilder()
    .withRefId("/ref/feeture")
    .withSetting(SETTING_BRANCH_REJECTION_REGEXP, "/ref/(master|feature)$")
    .withSetting(SETTING_BRANCH_REJECTION_REGEXP_MESSAGE, "not ok")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "/ref/feeture e2bc4ed003 -> af35d5c1a4  - Branch: /ref/feeture, /ref/(master|feature)$   not ok") //
    .wasRejected();
 }

 @Test
 public void testThatBranchCanBeRejectedByRegexpEvenIfNoCommitsArePushedUnlessDelete() throws IOException {
  refChangeBuilder() //
    .withRefId("/ref/feeture") //
    .withType(DELETE) //
    .withSetting(SETTING_BRANCH_REJECTION_REGEXP, "/ref/(master|feature)$") //
    .withSetting(SETTING_BRANCH_REJECTION_REGEXP_MESSAGE, "not ok") //
    .build() //
    .run() //
    .wasAccepted();
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
    .build() //
    .run() //
    .hasNoOutput() //
    .wasAccepted();
 }
}
