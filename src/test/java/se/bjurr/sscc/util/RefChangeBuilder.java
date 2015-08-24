package se.bjurr.sscc.util;

import static com.atlassian.fugue.Throwables.propagate;
import static com.atlassian.stash.repository.RefChangeType.ADD;
import static com.atlassian.stash.user.Permission.REPO_ADMIN;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.io.Resources.getResource;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.bjurr.sscc.JqlValidator.setJiraClient;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_ACCEPT;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_MATCH;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_RULE_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_RULE_REGEXP;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.mockito.Matchers;

import se.bjurr.sscc.ChangeSetsService;
import se.bjurr.sscc.JiraClient;
import se.bjurr.sscc.ResultsCallable;
import se.bjurr.sscc.SsccPreReceiveRepositoryHook;
import se.bjurr.sscc.SsccRepositoryMergeRequestCheck;
import se.bjurr.sscc.SsccUserAdminService;
import se.bjurr.sscc.SsccUserAdminServiceImpl;
import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.scm.pull.MergeRequest;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsBuilder;
import com.atlassian.stash.user.DetailedUser;
import com.atlassian.stash.user.EscalatedSecurityContext;
import com.atlassian.stash.user.SecurityService;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.StashUserVisitor;
import com.atlassian.stash.user.UserAdminService;
import com.atlassian.stash.user.UserType;
import com.atlassian.stash.util.Operation;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.google.common.base.Function;
import com.google.common.io.Resources;

public class RefChangeBuilder {
 public static final String JIRA_REGEXP = "((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)";
 public static final String JIRA_RESPONSE_EMPTY = "jiraResponseEmpty.json";
 public static final String JIRA_RESPONSE_ONE = "jiraResponseOne.json";
 public static final String JIRA_RESPONSE_TWO = "jiraResponseTwo.json";

 public static RefChangeBuilder refChangeBuilder() {
  try {
   return new RefChangeBuilder();
  } catch (Throwable t) {
   propagate(t, RuntimeException.class);
   return null;
  }
 }

 private final ChangeSetsService changeSetService;
 private final UserAdminService userAdminService;
 private String fromHash = "e2bc4ed00386fafe00100738f739b9f29c9f4beb";
 private final SsccPreReceiveRepositoryHook hook;
 private final SsccRepositoryMergeRequestCheck mergeHook;

 private final HookResponse hookResponse;
 private final List<SSCCChangeSet> newChangesets;
 private final OutputStream outputAll = newOutputStream();
 private final PrintWriter printWriterReject = new PrintWriter(outputAll);
 private final PrintWriter printWriterStandard = new PrintWriter(outputAll);

 private RefChange refChange;
 private String refId = "refs/heads/master";
 private final RepositoryHookContext repositoryHookContext;
 private final Map<String, String> jiraJsonResponses = newHashMap();
 private final Settings settings;
 private final StashAuthenticationContext stashAuthenticationContext;
 private final StashUser stashUser;
 private String toHash = "af35d5c1a435d4f323b4e01775fa90a3eae652b3";
 private RefChangeType type = ADD;
 private Boolean wasAccepted = null;
 private ApplicationLinkService applicationLinkService;
 private SsccUserAdminService ssccUserAdminService;
 private final List<DetailedUser> detailedUsers = newArrayList();
 private boolean prWasAccepted;
 private String prSummary;
 private String prMessage;
 private RepositoryHookService repositoryHookService;
 private SecurityService securityService;

 @SuppressWarnings("unchecked")
 private RefChangeBuilder() throws Throwable {
  setJiraClient(new JiraClient() {
   @Override
   protected String invokeJira(ApplicationLinkService applicationLinkService, String jqlCheckQuery)
     throws UnsupportedEncodingException, ResponseException, CredentialsRequiredException {
    if (jiraJsonResponses.containsKey(jqlCheckQuery)) {
     return jiraJsonResponses.get(jqlCheckQuery);
    }
    throw new RuntimeException("No faked response for: \"" + jqlCheckQuery + "\"");
   }
  });

  newChangesets = newArrayList();
  settings = mock(Settings.class);
  this.repositoryHookContext = mock(RepositoryHookContext.class);
  when(repositoryHookContext.getSettings()).thenReturn(settings);
  this.changeSetService = mock(ChangeSetsService.class);
  this.stashAuthenticationContext = mock(StashAuthenticationContext.class);
  this.userAdminService = mock(UserAdminService.class);
  this.ssccUserAdminService = new SsccUserAdminServiceImpl(userAdminService);
  this.hook = new SsccPreReceiveRepositoryHook(changeSetService, stashAuthenticationContext, applicationLinkService,
    ssccUserAdminService);
  this.hook.setHookName("");
  PluginSettingsFactory pluginSettingsFactory = mock(PluginSettingsFactory.class);
  repositoryHookService = mock(RepositoryHookService.class);
  PluginSettings pluginSettings = mock(PluginSettings.class);
  when(pluginSettingsFactory.createGlobalSettings()).thenReturn(pluginSettings);
  HashMap<String, Object> map = new HashMap<String, Object>();
  when(pluginSettingsFactory.createGlobalSettings().get(Matchers.anyString())).thenReturn(map);
  SettingsBuilder settingsBuilder = mock(SettingsBuilder.class);
  when(repositoryHookService.createSettingsBuilder()).thenReturn(settingsBuilder);
  when(repositoryHookService.createSettingsBuilder().addAll(Matchers.anyMap())).thenReturn(settingsBuilder);
  when(repositoryHookService.createSettingsBuilder().build()).thenReturn(settings);
  securityService = mock(SecurityService.class);
  EscalatedSecurityContext escalatedSecurityContext = mock(EscalatedSecurityContext.class);
  when(escalatedSecurityContext.call(Matchers.any(Operation.class))).thenReturn(settings);
  when(securityService.withPermission(REPO_ADMIN, "Retrieving settings")).thenReturn(escalatedSecurityContext);
  this.mergeHook = new SsccRepositoryMergeRequestCheck(changeSetService, stashAuthenticationContext,
    applicationLinkService, ssccUserAdminService, pluginSettingsFactory, repositoryHookService, securityService);
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
      Matchers.eq(refId), Matchers.eq(type), Matchers.eq(fromHash), Matchers.eq(toHash))).thenReturn(newChangesets);
  when(changeSetService.getNewChangeSets(Matchers.any(SSCCSettings.class), Matchers.any(PullRequest.class)))
    .thenReturn(newChangesets);
  when(userAdminService.findUsers(Matchers.any(PageRequest.class))).thenReturn(new Page<DetailedUser>() {

   // This method is not available in Sash 3, but in 2.12.0, should not override
   public String getFilter() {
    return null;
   }

   @Override
   public <E> Page<E> transform(Function<? super DetailedUser, ? extends E> arg0) {
    getFilter(); // Ensure save-actions does not remove the method
    return null;
   }

   @Override
   public Iterable<DetailedUser> getValues() {
    return detailedUsers;
   }

   @Override
   public int getStart() {
    return 0;
   }

   @Override
   public int getSize() {
    return detailedUsers.size();
   }

   @Override
   public SortedMap<Integer, DetailedUser> getOrdinalIndexedValues() {
    return null;
   }

   @Override
   public PageRequest getNextPageRequest() {
    return null;
   }

   @Override
   public int getLimit() {
    return 0;
   }

   @Override
   public boolean getIsLastPage() {
    return false;
   }
  });
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

 @SuppressWarnings("deprecation")
 public RefChangeBuilder runPullRequest() throws IOException {
  checkNotNull(refChange, "do 'throwing' or 'build' before.");
  mergeHook.setChangesetsService(changeSetService);
  this.mergeHook.setResultsCallback(new ResultsCallable() {
   @Override
   public void report(boolean isAccepted, String summaryParam, String messageParam) {
    prWasAccepted = isAccepted;
    prSummary = summaryParam;
    prMessage = messageParam;
   }
  });
  when(repositoryHookService.getSettings(Matchers.any(Repository.class), Matchers.anyString())).thenReturn(settings);
  Repository repository = mock(Repository.class);
  MergeRequest mergeRequest = mock(MergeRequest.class);
  PullRequest pullRequest = mock(PullRequest.class);
  PullRequestRef fromRef = mock(PullRequestRef.class);
  PullRequestRef toRef = mock(PullRequestRef.class);
  when(mergeRequest.getPullRequest()).thenReturn(pullRequest);
  when(mergeRequest.getPullRequest().getFromRef()).thenReturn(fromRef);
  when(mergeRequest.getPullRequest().getFromRef().getId()).thenReturn(refId);
  when(mergeRequest.getPullRequest().getFromRef().getLatestChangeset()).thenReturn(fromHash);
  when(mergeRequest.getPullRequest().getFromRef().getRepository()).thenReturn(repository);
  when(mergeRequest.getPullRequest().getToRef()).thenReturn(toRef);
  when(mergeRequest.getPullRequest().getToRef().getLatestChangeset()).thenReturn(toHash);
  this.mergeHook.check(mergeRequest);
  return this;
 }

 public RefChangeBuilder throwing(IOException ioException) throws IOException {
  refChange = newRefChange();
  when(
    changeSetService.getNewChangeSets(Matchers.any(SSCCSettings.class), Matchers.any(Repository.class),
      Matchers.any(String.class), Matchers.any(RefChangeType.class), Matchers.any(String.class),
      Matchers.any(String.class))).thenThrow(ioException);
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

 public RefChangeBuilder hasTrimmedPrSummary(String summary) {
  assertEquals(summary.trim().replaceAll("\n", " "), prSummary.trim().replaceAll("\n", " "));
  return this;
 }

 public RefChangeBuilder hasTrimmedPrPrintOut(String printOut) {
  assertEquals(printOut.trim().replaceAll("\n", " "), prMessage.trim().replaceAll("\n", " "));
  return this;
 }

 public RefChangeBuilder prWasAccepted() {
  assertTrue("Pull request was not accepted", prWasAccepted);
  return this;
 }

 public RefChangeBuilder prWasRejected() {
  assertFalse("Pull request was not rejected", prWasAccepted);
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
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA");
 }

 public RefChangeBuilder withGroupAcceptingJirasAndAnotherRejectingInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "You need to specity JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
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
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withGroupAcceptingOnlyJiraAndAnotherGroupAcceptingOnlyInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "You need to specity JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
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
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withGroupShowingMessageForAnyCommitContainingAtLeastOneJira() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.SHOW_MESSAGE.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for specifying a Jira =)") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA");
 }

 public RefChangeBuilder withGroupShowingMessageToAllCommitsNotContainingJiraOrInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.SHOW_MESSAGE.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.NONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for not specifying a Jira or INC =)") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withGroupShowingMessageToEveryCommitContainingJiraOrInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SSCCGroup.Accept.SHOW_MESSAGE.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SSCCGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for specifying a Jira and INC =)") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC[0-9]*") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withHookNameVersion(String hookNameVersion) {
  hook.setHookName(hookNameVersion);
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

 public RefChangeBuilder withStashName(String name) {
  when(stashUser.getName()).thenReturn(name);
  return this;
 }

 public RefChangeBuilder withStashEmail(String email) {
  when(stashUser.getEmailAddress()).thenReturn(email);
  return this;
 }

 public RefChangeBuilder withStashUserType(UserType type) {
  when(stashUser.getType()).thenReturn(type);
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

 public RefChangeBuilder fakeJiraResponse(String jqlQuery, String responseFileName) throws IOException {
  jiraJsonResponses.put(jqlQuery, Resources.toString(getResource(responseFileName), UTF_8));
  return this;
 }

 public RefChangeBuilder withUserInStash(final String displayName, final String name, final String email) {
  detailedUsers.add(new DetailedUser() {

   @Override
   public String getName() {
    return name;
   }

   @Override
   public String getEmailAddress() {
    return email;
   }

   @Override
   public boolean isActive() {
    return false;
   }

   @Override
   public UserType getType() {
    return null;
   }

   @Override
   public String getSlug() {
    return null;
   }

   @Override
   public Integer getId() {
    return null;
   }

   @Override
   public String getDisplayName() {
    return displayName;
   }

   @Override
   public <T> T accept(StashUserVisitor<T> arg0) {
    return null;
   }

   @Override
   public boolean isMutableGroups() {
    return false;
   }

   @Override
   public boolean isMutableDetails() {
    getLastAuthenticationTimestamp(); // Ensure its not removed by save-action
    return false;
   }

   // Not available in Stash 2.12.0, should not use @override
   public Date getLastAuthenticationTimestamp() {
    return null;
   }

   @Override
   public String getDirectoryName() {
    return null;
   }

   // Not available in Stash 2.12.0, should not use @override
   public boolean isDeletable() {
    return false;
   }
  });
  return this;
 }
}
