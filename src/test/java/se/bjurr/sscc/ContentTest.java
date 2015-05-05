package se.bjurr.sscc;

import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_DIFF_REGEXP;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_DIFF_REGEXP_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_SIZE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_SIZE_MESSAGE;
import static se.bjurr.sscc.util.RefChangeBuilder.refChangeBuilder;

import java.io.IOException;

import org.junit.Test;

public class ContentTest {
 private static final String UNRESOLVED_MERGE = "<<<<<<<.*?=======.*?>>>>>>>";

 @Test
 public void testContentWithUnresolvedMergeCanBeRejected() throws IOException {
  refChangeBuilder()
    .withChangeSet(changeSetBuilder().withId("1").withMessage("").withDiff("" + //
      "line 1\n" + //
      "+line 2\n" + //
      "+<<<<<<<\n" + //
      "+=======\n" + //
      "+>>>>>>>\n" + //
      "line 3\n" //
    ).build())
    .withSetting(SETTING_DIFF_REGEXP, UNRESOLVED_MERGE)
    .withSetting(SETTING_DIFF_REGEXP_MESSAGE, "Unresolved merge found!")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>>   - <<<<<<<.*?=======.*?>>>>>>>: <<<<<<< +======= +>>>>>>>      Unresolved merge found!")
    .wasRejected();
 }

 @Test
 public void testContentWithoutUnresolvedMergeCanBeRejected() throws IOException {
  refChangeBuilder().withChangeSet(changeSetBuilder().withId("1").withMessage("").withDiff("" + //
    "line 1\n" + //
    "line 2\n" + //
    "1 <<<<<<< 2\n" + //
    "line 3\n" //
  ).build()).withSetting(SETTING_DIFF_REGEXP, UNRESOLVED_MERGE)
    .withSetting(SETTING_DIFF_REGEXP_MESSAGE, "Unresolved merge found!").build().run().wasAccepted();
 }

 @Test
 public void testContentWithOneLargeFileSizeCanBeRejected() throws IOException {
  refChangeBuilder()
    .withChangeSet(changeSetBuilder().withId("1").withMessage("").withSize("someFile", 15000 * 1024L).build())
    .withSetting(SETTING_SIZE, "10000")
    .withSetting(SETTING_SIZE_MESSAGE, "To large!")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>>   - someFile 15000kb > 10000kb   To large!")
    .wasRejected();
 }

 @Test
 public void testContentWithTwoLargeFileSizeCanBeRejected() throws IOException {
  refChangeBuilder()
    .withChangeSet(
      changeSetBuilder().withId("1").withMessage("").withSize("someFile", 15000 * 1024L)
        .withSize("someOtherFile", 16000 * 1024L).build())
    .withSetting(SETTING_SIZE, "10000")
    .withSetting(SETTING_SIZE_MESSAGE, "To large!")
    .build()
    .run()
    .hasTrimmedFlatOutput(
      "refs/heads/master e2bc4ed003 -> af35d5c1a4   1 Tomas <my@email.com> >>>   - someFile 15000kb > 10000kb   To large!  - someOtherFile 16000kb > 10000kb   To large!")
    .wasRejected();
 }

 @Test
 public void testContentWithSizeCanBeAccepted() throws IOException {
  refChangeBuilder()
    .withChangeSet(changeSetBuilder().withId("1").withMessage("").withSize("someFile", 15000 * 1024L).build())
    .withSetting(SETTING_SIZE, "15001").withSetting(SETTING_SIZE_MESSAGE, "To large!").build().run().wasAccepted();
 }
}
