package se.bjurr.sscc.util;

import static com.atlassian.stash.repository.RefChangeType.UPDATE;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Resources.getResource;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_ACCEPT;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_MATCH;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_RULE_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_RULE_REGEXP;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.mockito.Matchers;

import se.bjurr.sscc.ChangeSetsService;
import se.bjurr.sscc.SsccPreReceiveRepositoryHook;
import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.user.StashUser;
import com.google.common.io.Resources;

public class RefChangeBuilder {
 public static RefChangeBuilder refChangeBuilder() {
  return new RefChangeBuilder();
 }

 private final ChangeSetsService changeSetService;
 private String fromHash = "e2bc4ed00386fafe00100738f739b9f29c9f4beb";
 private final SsccPreReceiveRepositoryHook hook;

 private final HookResponse hookResponse;
 private final List<SSCCChangeSet> newChangesets;
 private final OutputStream outputAll = newOutputStream();
 private final PrintWriter printWriterReject = new PrintWriter(outputAll);
 private final PrintWriter printWriterStandard = new PrintWriter(outputAll);

 private RefChange refChange;
 private String refId = "refs/heads/master";
 private final RepositoryHookContext repositoryHookContext;

 private final Settings settings;
 private final StashAuthenticationContext stashAuthenticationContext;
 private final StashUser stashUser;
 private String toHash = "af35d5c1a435d4f323b4e01775fa90a3eae652b3";
 private RefChangeType type = UPDATE;
 private Boolean wasAccepted = null;

 private RefChangeBuilder() {
  newChangesets = newArrayList();
  settings = mock(Settings.class);
  this.repositoryHookContext = mock(RepositoryHookContext.class);
  when(repositoryHookContext.getSettings()).thenReturn(settings);
  this.changeSetService = mock(ChangeSetsService.class);
  this.stashAuthenticationContext = mock(StashAuthenticationContext.class);
  this.hook = new SsccPreReceiveRepositoryHook(changeSetService, stashAuthenticationContext);
  this.hook.setHookNameVersion("");
  hookResponse = mock(HookResponse.class);
  when(hookResponse.out()).thenReturn(printWriterStandard);
  when(hookResponse.err()).thenReturn(printWriterReject);
  stashUser = mock(StashUser.class);
  when(stashAuthenticationContext.getCurrentUser()).thenReturn(stashUser);
 }

 public RefChangeBuilder build() throws IOException {
  refChange = newRefChange();
  when(
    changeSetService.getNewChangeSets(Matchers.any(SSCCSettings.class), Matchers.any(Repository.class),
      Matchers.any(RefChange.class))).thenReturn(newChangesets);
  return this;
 }

 public String getOutputAll() {
  return outputAll.toString();
 }

 public RefChangeBuilder hasNoOutput() {
  assertTrue("Expected output to be empty, but was\"" + getOutputAll() + "\"", getOutputAll().isEmpty());
  return this;
 }

 public RefChangeBuilder hasOutput(String output) {
  checkNotNull(wasAccepted, "do 'run' before.");
  assertEquals(output, getOutputAll());
  return this;
 }

 public RefChangeBuilder hasOutputFrom(String filename) throws IOException {
  return hasOutput(Resources.toString(getResource(filename), UTF_8));
 }

 public RefChangeBuilder hasTrimmedFlatOutput(String output) {
  checkNotNull(wasAccepted, "do 'run' before.");
  assertEquals(output.trim().replaceAll("\n", " "), getOutputAll().trim().replaceAll("\n", " "));
  return this;

 }

 private OutputStream newOutputStream() {
  return new OutputStream() {
   private final StringBuilder string = new StringBuilder();

   @Override
   public String toString() {
    return this.string.toString();
   }

   @Override
   public void write(int b) throws IOException {
    this.string.append((char) b);
   }
  };
 }

 private RefChange newRefChange() {
  final RefChange refChange = new RefChange() {

   @Override
   public String getFromHash() {
    return fromHash;
   }

   @Override
   public String getRefId() {
    return refId;
   }

   @Override
   public String getToHash() {
    return toHash;
   }

   @Override
   public RefChangeType getType() {
    return type;
   }
  };
  return refChange;
 }

 public RefChangeBuilder run() throws IOException {
  checkNotNull(refChange, "do 'throwing' or 'build' before.");
  hook.setChangesetsService(changeSetService);
  wasAccepted = hook.onReceive(repositoryHookContext, newArrayList(refChange), hookResponse);
  printWriterReject.flush();
  printWriterStandard.flush();
  return this;
 }

 public RefChangeBuilder throwing(IOException ioException) throws IOException {
  refChange = newRefChange();
  when(
    changeSetService.getNewChangeSets(Matchers.any(SSCCSettings.class), Matchers.any(Repository.class),
      Matchers.any(RefChange.class))).thenThrow(ioException);
  return this;
 }

 public RefChangeBuilder wasAccepted() {
  assertEquals("Expected accepted", TRUE, wasAccepted);
  return this;
 }

 public RefChangeBuilder wasRejected() {
  assertEquals("Expected rejection", FALSE, wasAccepted);
  return this;
 }

 public RefChangeBuilder withChangeSet(SSCCChangeSet changeSet) {
  newChangesets.add(changeSet);
  return this;
 }

 public RefChangeBuilder withFromHash(String fromHash) {
  this.fromHash = fromHash;
  return this;
 }

 public RefChangeBuilder withGroupAcceptingAtLeastOneJira() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "You need to specity an issue") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA");
 }

 public RefChangeBuilder withGroupAcceptingJirasAndAnotherRejectingInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "You need to specity JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_GROUP_ACCEPT + "[1]", SSCCGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[1]", SSCCGroup.Match.NONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[1]", "Dont include INC") //
    .withSetting(SETTING_RULE_REGEXP + "[1][0]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[1][0]", "Incident, INC");
 }

 public RefChangeBuilder withGroupAcceptingOnlyBothJiraAndIncInEachCommit() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "You need to specity JIRA and INC") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withGroupAcceptingOnlyJiraAndAnotherGroupAcceptingOnlyInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "You need to specity JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_GROUP_ACCEPT + "[1]", SSCCGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[1]", SSCCGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[1]", "You need to specity INC") //
    .withSetting(SETTING_RULE_REGEXP + "[1][0]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[1][0]", "Incident, INC");
 }

 public RefChangeBuilder withGroupRejectingAnyCommitContainingJiraOrInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.NONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Do not specify issues") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withGroupShowingMessageForAnyCommitContainingAtLeastOneJira() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.SHOW_MESSAGE.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for specifying a Jira =)") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA");
 }

 public RefChangeBuilder withGroupShowingMessageToAllCommitsNotContainingJiraOrInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.SHOW_MESSAGE.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.NONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for not specifying a Jira or INC =)") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withGroupShowingMessageToEveryCommitContainingJiraOrInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.SHOW_MESSAGE.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for specifying a Jira and INC =)") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC[0-9]*") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withHookNameVersion(String hookNameVersion) {
  hook.setHookNameVersion(hookNameVersion);
  return this;
 }

 public RefChangeBuilder withRefChange(RefChange refChange) {
  this.refChange = refChange;
  return this;
 }

 public RefChangeBuilder withRefId(String refId) {
  this.refId = refId;
  return this;
 }

 public RefChangeBuilder withSetting(String field, Boolean value) {
  when(settings.getBoolean(field)).thenReturn(value);
  return this;
 }

 public RefChangeBuilder withSetting(String field, String value) {
  when(settings.getString(field)).thenReturn(value);
  return this;
 }

 public RefChangeBuilder withStashDisplayName(String name) {
  when(stashUser.getDisplayName()).thenReturn(name);
  return this;
 }

 public RefChangeBuilder withStashEmail(String email) {
  when(stashUser.getEmailAddress()).thenReturn(email);
  return this;
 }

 public RefChangeBuilder withToHash(String toHash) {
  this.toHash = toHash;
  return this;
 }

 public RefChangeBuilder withType(RefChangeType type) {
  this.type = type;
  return this;
 }
}
