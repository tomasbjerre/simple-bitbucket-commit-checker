package se.bjurr.sbcc.util;

import static com.atlassian.bitbucket.permission.Permission.REPO_ADMIN;
import static com.atlassian.bitbucket.repository.RefChangeType.ADD;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
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
import static se.bjurr.sbcc.JqlValidator.setJiraClient;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_GROUP_ACCEPT;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_GROUP_MATCH;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_GROUP_MESSAGE;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_RULE_MESSAGE;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_RULE_REGEXP;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.mockito.Matchers;

import se.bjurr.sbcc.ChangeSetsService;
import se.bjurr.sbcc.JiraClient;
import se.bjurr.sbcc.ResultsCallable;
import se.bjurr.sbcc.SbccPreReceiveRepositoryHook;
import se.bjurr.sbcc.SbccRepositoryMergeRequestCheck;
import se.bjurr.sbcc.SbccUserAdminService;
import se.bjurr.sbcc.SbccUserAdminServiceImpl;
import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.settings.SbccGroup;
import se.bjurr.sbcc.settings.SbccSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.hook.HookResponse;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.RefType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scm.pull.MergeRequest;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsBuilder;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.DetailedUser;
import com.atlassian.bitbucket.user.EscalatedSecurityContext;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.user.UserAdminService;
import com.atlassian.bitbucket.user.UserType;
import com.atlassian.bitbucket.util.Operation;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
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
   propagate(t);
   return null;
  }
 }

 private final ChangeSetsService changeSetService;
 private final UserAdminService userAdminService;
 private String fromHash = "e2bc4ed00386fafe00100738f739b9f29c9f4beb";
 private final SbccPreReceiveRepositoryHook hook;
 private final SbccRepositoryMergeRequestCheck mergeHook;

 private final HookResponse hookResponse;
 private final List<SbccChangeSet> newChangesets;
 private final OutputStream outputAll = newOutputStream();
 private final PrintWriter printWriterReject = new PrintWriter(outputAll);
 private final PrintWriter printWriterStandard = new PrintWriter(outputAll);

 private RefChange refChange;
 private String refId = "refs/heads/master";
 private final RepositoryHookContext repositoryHookContext;
 private final Map<String, String> jiraJsonResponses = newHashMap();
 private final Settings settings;
 private final AuthenticationContext bitbucketAuthenticationContext;
 private final ApplicationUser bitbucketUser;
 private String toHash = "af35d5c1a435d4f323b4e01775fa90a3eae652b3";
 private RefChangeType type = ADD;
 private Boolean wasAccepted = null;
 private ApplicationLinkService applicationLinkService;
 private SbccUserAdminService sbccUserAdminService;
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
  this.bitbucketAuthenticationContext = mock(AuthenticationContext.class);
  this.userAdminService = mock(UserAdminService.class);
  this.sbccUserAdminService = new SbccUserAdminServiceImpl(userAdminService);
  this.hook = new SbccPreReceiveRepositoryHook(changeSetService, bitbucketAuthenticationContext,
    applicationLinkService, sbccUserAdminService);
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
  Operation<Object, RuntimeException> operation = Matchers.any(Operation.class);
  when(escalatedSecurityContext.call(operation)).thenReturn(settings);
  when(securityService.withPermission(REPO_ADMIN, "Retrieving settings")).thenReturn(escalatedSecurityContext);
  this.mergeHook = new SbccRepositoryMergeRequestCheck(changeSetService, bitbucketAuthenticationContext,
    applicationLinkService, sbccUserAdminService, pluginSettingsFactory, repositoryHookService, securityService);
  hookResponse = mock(HookResponse.class);
  when(hookResponse.out()).thenReturn(printWriterStandard);
  when(hookResponse.err()).thenReturn(printWriterReject);
  bitbucketUser = mock(ApplicationUser.class);
  when(bitbucketAuthenticationContext.getCurrentUser()).thenReturn(bitbucketUser);
 }

 public RefChangeBuilder build() throws IOException {
  refChange = newRefChange();
  when(
    changeSetService.getNewChangeSets(Matchers.any(SbccSettings.class), Matchers.any(Repository.class),
      Matchers.eq(refId), Matchers.eq(type), Matchers.eq(fromHash), Matchers.eq(toHash))).thenReturn(newChangesets);
  when(changeSetService.getNewChangeSets(Matchers.any(SbccSettings.class), Matchers.any(PullRequest.class)))
    .thenReturn(newChangesets);
  when(userAdminService.findUsers(Matchers.any(PageRequest.class))).thenReturn(new Page<DetailedUser>() {

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

   @Override
   public <E> Page<E> transform(java.util.function.Function<? super DetailedUser, ? extends E> arg0) {
    return null;
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

   @Override
   public MinimalRef getRef() {
    return new MinimalRef() {

     @Override
     public RefType getType() {
      return null;
     }

     @Override
     public String getId() {
      return refId;
     }

     @Override
     public String getDisplayId() {
      return null;
     }
    };
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
  when(mergeRequest.getPullRequest().getFromRef().getLatestCommit()).thenReturn(fromHash);
  when(mergeRequest.getPullRequest().getFromRef().getRepository()).thenReturn(repository);
  when(mergeRequest.getPullRequest().getToRef()).thenReturn(toRef);
  when(mergeRequest.getPullRequest().getToRef().getLatestCommit()).thenReturn(toHash);
  this.mergeHook.check(mergeRequest);
  return this;
 }

 public RefChangeBuilder throwing(IOException ioException) throws IOException {
  refChange = newRefChange();
  when(
    changeSetService.getNewChangeSets(Matchers.any(SbccSettings.class), Matchers.any(Repository.class),
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

 public RefChangeBuilder withChangeSet(SbccChangeSet changeSet) {
  newChangesets.add(changeSet);
  return this;
 }

 public RefChangeBuilder withFromHash(String fromHash) {
  this.fromHash = fromHash;
  return this;
 }

 public RefChangeBuilder withGroupAcceptingAtLeastOneJira() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.ONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "You need to specity an issue") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA");
 }

 public RefChangeBuilder withGroupAcceptingJirasAndAnotherRejectingInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "You need to specity JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_GROUP_ACCEPT + "[1]", SbccGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[1]", SbccGroup.Match.NONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[1]", "Dont include INC") //
    .withSetting(SETTING_RULE_REGEXP + "[1][0]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[1][0]", "Incident, INC");
 }

 public RefChangeBuilder withGroupAcceptingOnlyBothJiraAndIncInEachCommit() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "You need to specity JIRA and INC") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withGroupAcceptingOnlyJiraAndAnotherGroupAcceptingOnlyInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "You need to specity JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_GROUP_ACCEPT + "[1]", SbccGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[1]", SbccGroup.Match.ALL.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[1]", "You need to specity INC") //
    .withSetting(SETTING_RULE_REGEXP + "[1][0]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[1][0]", "Incident, INC");
 }

 public RefChangeBuilder withGroupRejectingAnyCommitContainingJiraOrInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.ACCEPT.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.NONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Do not specify issues") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withGroupShowingMessageForAnyCommitContainingAtLeastOneJira() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.SHOW_MESSAGE.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.ONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for specifying a Jira =)") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA");
 }

 public RefChangeBuilder withGroupShowingMessageToAllCommitsNotContainingJiraOrInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.SHOW_MESSAGE.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.NONE.toString()) //
    .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for not specifying a Jira or INC =)") //
    .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
    .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
    .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC") //
    .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
 }

 public RefChangeBuilder withGroupShowingMessageToEveryCommitContainingJiraOrInc() {
  return this.withSetting(SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.SHOW_MESSAGE.toString()) //
    .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.ALL.toString()) //
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

 public RefChangeBuilder withBitbucketDisplayName(String name) {
  when(bitbucketUser.getDisplayName()).thenReturn(name);
  return this;
 }

 public RefChangeBuilder withBitbucketName(String name) {
  when(bitbucketUser.getName()).thenReturn(name);
  return this;
 }

 public RefChangeBuilder withBitbucketEmail(String email) {
  when(bitbucketUser.getEmailAddress()).thenReturn(email);
  return this;
 }

 public RefChangeBuilder withBitbucketUserType(UserType type) {
  when(bitbucketUser.getType()).thenReturn(type);
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

 public RefChangeBuilder withUserInBitbucket(final String displayName, final String name, final String email) {
  DetailedUser mockedApplicationUser = mock(DetailedUser.class);
  when(mockedApplicationUser.getName()).thenReturn(name);
  when(mockedApplicationUser.getEmailAddress()).thenReturn(email);
  when(mockedApplicationUser.getSlug()).thenReturn("slug");
  when(mockedApplicationUser.getId()).thenReturn(0);
  when(mockedApplicationUser.getDisplayName()).thenReturn(displayName);
  detailedUsers.add(mockedApplicationUser);
  return this;
 }
}
