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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static se.bjurr.sbcc.JqlValidator.setJiraClient;
import static se.bjurr.sbcc.SbccRepositoryHook.PR_REJECT_DEFAULT_MSG;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_GROUP_ACCEPT;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_GROUP_MATCH;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_GROUP_MESSAGE;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_RULE_MESSAGE;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_RULE_REGEXP;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;

import se.bjurr.sbcc.JiraClient;
import se.bjurr.sbcc.SbccRepositoryHook;
import se.bjurr.sbcc.SbccUserAdminService;
import se.bjurr.sbcc.commits.ChangeSetsService;
import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.settings.SbccGroup;
import se.bjurr.sbcc.settings.SbccSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.hook.ScmHookDetails;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.RefType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsBuilder;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.EscalatedSecurityContext;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.user.UserType;
import com.atlassian.bitbucket.util.Operation;
import com.atlassian.bitbucket.util.Page;
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
    } catch (final Throwable t) {
      propagate(t);
      return null;
    }
  }

  private ApplicationLinkService applicationLinkService;
  private final AuthenticationContext bitbucketAuthenticationContext;
  private final ApplicationUser bitbucketUser;
  private final ChangeSetsService changeSetService;

  private String fromHash = "e2bc4ed00386fafe00100738f739b9f29c9f4beb";
  private final SbccRepositoryHook hook;
  private final Map<String, String> jiraJsonResponses = newHashMap();

  private final List<SbccChangeSet> newChangesets;
  private String outputAll = null;
  private RefChange refChange;
  private String refId = "refs/heads/master";
  private final RepositoryHookContext repositoryHookContext;
  private RepositoryHookService repositoryHookService;
  private SbccUserAdminService sbccUserAdminService;
  private SecurityService securityService;
  private final Settings settings;
  private String toHash = "af35d5c1a435d4f323b4e01775fa90a3eae652b3";
  private RefChangeType type = ADD;
  private Boolean wasAccepted = null;

  @Captor private ArgumentCaptor<Map<String, ?>> mapCaptor;
  private RepositoryHookResult repoHookResponse;

  @SuppressWarnings("unchecked")
  private RefChangeBuilder() throws Throwable {
    initMocks(this);
    setJiraClient(
        new JiraClient() {
          @Override
          protected String invokeJira(
              final ApplicationLinkService applicationLinkService, final String jqlCheckQuery)
              throws UnsupportedEncodingException, ResponseException, CredentialsRequiredException {
            if (RefChangeBuilder.this.jiraJsonResponses.containsKey(jqlCheckQuery)) {
              return RefChangeBuilder.this.jiraJsonResponses.get(jqlCheckQuery);
            }
            throw new RuntimeException("No faked response for: \"" + jqlCheckQuery + "\"");
          }
        });

    this.newChangesets = newArrayList();
    this.settings = mock(Settings.class);
    this.repositoryHookContext = mock(RepositoryHookContext.class);
    when(this.repositoryHookContext.getSettings()).thenReturn(this.settings);
    this.changeSetService = mock(ChangeSetsService.class);
    this.bitbucketAuthenticationContext = mock(AuthenticationContext.class);
    this.sbccUserAdminService = mock(SbccUserAdminService.class);

    this.securityService = mock(SecurityService.class);
    final EscalatedSecurityContext escalatedSecurityContext = mock(EscalatedSecurityContext.class);
    final Operation<Object, RuntimeException> operation = ArgumentMatchers.any(Operation.class);
    when(escalatedSecurityContext.call(operation)).thenReturn(this.settings);
    when(this.securityService.withPermission(REPO_ADMIN, "Retrieving settings"))
        .thenReturn(escalatedSecurityContext);

    this.hook =
        new SbccRepositoryHook(
            this.changeSetService,
            this.bitbucketAuthenticationContext,
            this.applicationLinkService,
            this.sbccUserAdminService,
            securityService,
            repositoryHookService);
    this.hook.setHookName("");
    final PluginSettingsFactory pluginSettingsFactory = mock(PluginSettingsFactory.class);
    this.repositoryHookService = mock(RepositoryHookService.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    when(pluginSettingsFactory.createGlobalSettings()).thenReturn(pluginSettings);
    final HashMap<String, Object> map = new HashMap<>();
    when(pluginSettingsFactory.createGlobalSettings().get(ArgumentMatchers.anyString()))
        .thenReturn(map);
    final SettingsBuilder settingsBuilder = mock(SettingsBuilder.class);
    when(this.repositoryHookService.createSettingsBuilder()).thenReturn(settingsBuilder);

    when(this.repositoryHookService.createSettingsBuilder().addAll(mapCaptor.capture()))
        .thenReturn(settingsBuilder);
    when(this.repositoryHookService.createSettingsBuilder().build()).thenReturn(this.settings);
    this.bitbucketUser = mock(ApplicationUser.class);
    when(this.bitbucketAuthenticationContext.getCurrentUser()).thenReturn(this.bitbucketUser);
  }

  public RefChangeBuilder build() throws IOException {
    this.refChange = newRefChange();
    when(this.changeSetService.getNewChangeSets(
            ArgumentMatchers.any(SbccSettings.class),
            ArgumentMatchers.any(Repository.class),
            ArgumentMatchers.eq(this.refId),
            ArgumentMatchers.eq(this.type),
            ArgumentMatchers.eq(this.fromHash),
            ArgumentMatchers.eq(this.toHash)))
        .thenReturn(this.newChangesets);
    return this;
  }

  public RefChangeBuilder fakeJiraResponse(final String jqlQuery, final String responseFileName)
      throws IOException {
    this.jiraJsonResponses.put(jqlQuery, Resources.toString(getResource(responseFileName), UTF_8));
    return this;
  }

  public String getOutputAll() {
    return this.outputAll;
  }

  public RefChangeBuilder hasNoOutput() {
    assertTrue(
        "Expected output to be empty, but was\"" + getOutputAll() + "\"", getOutputAll().isEmpty());
    return this;
  }

  public RefChangeBuilder hasOutput(final String output) {
    checkNotNull(this.wasAccepted, "do 'run' before.");
    assertEquals(output, getOutputAll());
    return this;
  }

  public RefChangeBuilder hasOutputFrom(final String filename) throws IOException {
    return hasOutput(Resources.toString(getResource(filename), UTF_8));
  }

  public RefChangeBuilder hasTrimmedFlatOutput(final String output) {
    checkNotNull(this.wasAccepted, "do 'run' before.");
    assertEquals(output.trim().replaceAll("\n", " "), getOutputAll().trim().replaceAll("\n", " "));
    return this;
  }

  public RefChangeBuilder run() throws IOException {
    checkNotNull(this.refChange, "do 'throwing' or 'build' before.");
    this.hook.setChangesetsService(this.changeSetService);
    StringWriter stringWriter = new StringWriter();
    PrintWriter scmHookDetailsOut = new PrintWriter(stringWriter);
    ScmHookDetails scmHookDetails = mock(ScmHookDetails.class);
    when(scmHookDetails.out()).thenReturn(scmHookDetailsOut);
    Repository repository = mock(Repository.class);

    repoHookResponse =
        this.hook.performChecks(newArrayList(this.refChange), scmHookDetails, repository);
    this.wasAccepted = repoHookResponse.isAccepted();
    this.outputAll = stringWriter.toString();
    if (!repoHookResponse.getVetoes().isEmpty()) {
      String vetoSummary = repoHookResponse.getVetoes().get(0).getSummaryMessage();
      assertEquals(PR_REJECT_DEFAULT_MSG, vetoSummary);
      String vetoDetail = repoHookResponse.getVetoes().get(0).getDetailedMessage();
      assertEquals("", this.outputAll);
      this.outputAll = vetoDetail;
    }
    return this;
  }

  public RefChangeBuilder throwing(final IOException ioException) throws IOException {
    this.refChange = newRefChange();
    when(this.changeSetService.getNewChangeSets(
            ArgumentMatchers.any(SbccSettings.class),
            ArgumentMatchers.any(Repository.class),
            ArgumentMatchers.any(String.class),
            ArgumentMatchers.any(RefChangeType.class),
            ArgumentMatchers.any(String.class),
            ArgumentMatchers.any(String.class)))
        .thenThrow(ioException);
    return this;
  }

  public RefChangeBuilder wasAccepted() {
    assertEquals("Expected accepted", TRUE, this.wasAccepted);
    return this;
  }

  public RefChangeBuilder wasRejected() {
    assertEquals("Expected rejection", FALSE, this.wasAccepted);
    return this;
  }

  public RefChangeBuilder withBitbucketDisplayName(final String name) {
    when(this.bitbucketUser.getDisplayName()).thenReturn(name);
    return this;
  }

  public RefChangeBuilder withBitbucketEmail(final String email) {
    when(this.bitbucketUser.getEmailAddress()).thenReturn(email);
    return this;
  }

  public RefChangeBuilder withBitbucketName(final String name) {
    when(this.bitbucketUser.getName()).thenReturn(name);
    return this;
  }

  public RefChangeBuilder withBitbucketUserSlug(final String name) {
    when(this.bitbucketUser.getSlug()).thenReturn(name);
    return this;
  }

  public RefChangeBuilder withBitbucketUserType(final UserType type) {
    when(this.bitbucketUser.getType()).thenReturn(type);
    return this;
  }

  public RefChangeBuilder withChangeSet(final SbccChangeSet changeSet) {
    this.newChangesets.add(changeSet);
    return this;
  }

  public RefChangeBuilder withFromHash(final String fromHash) {
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
    return this.withSetting(
            SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.SHOW_MESSAGE.toString()) //
        .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.ONE.toString()) //
        .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for specifying a Jira =)") //
        .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
        .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA");
  }

  public RefChangeBuilder withGroupShowingMessageToAllCommitsNotContainingJiraOrInc() {
    return this.withSetting(
            SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.SHOW_MESSAGE.toString()) //
        .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.NONE.toString()) //
        .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for not specifying a Jira or INC =)") //
        .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
        .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
        .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC") //
        .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
  }

  public RefChangeBuilder withGroupShowingMessageToEveryCommitContainingJiraOrInc() {
    return this.withSetting(
            SETTING_GROUP_ACCEPT + "[0]", SbccGroup.Accept.SHOW_MESSAGE.toString()) //
        .withSetting(SETTING_GROUP_MATCH + "[0]", SbccGroup.Match.ALL.toString()) //
        .withSetting(SETTING_GROUP_MESSAGE + "[0]", "Thanks for specifying a Jira and INC =)") //
        .withSetting(SETTING_RULE_REGEXP + "[0][0]", JIRA_REGEXP) //
        .withSetting(SETTING_RULE_MESSAGE + "[0][0]", "JIRA") //
        .withSetting(SETTING_RULE_REGEXP + "[0][1]", "INC[0-9]*") //
        .withSetting(SETTING_RULE_MESSAGE + "[0][1]", "Incident, INC");
  }

  public RefChangeBuilder withHookNameVersion(final String hookNameVersion) {
    this.hook.setHookName(hookNameVersion);
    return this;
  }

  public RefChangeBuilder withRefChange(final RefChange refChange) {
    this.refChange = refChange;
    return this;
  }

  public RefChangeBuilder withRefId(final String refId) {
    this.refId = refId;
    return this;
  }

  public RefChangeBuilder withSetting(final String field, final Boolean value) {
    when(this.settings.getBoolean(field)).thenReturn(value);
    return this;
  }

  public RefChangeBuilder withSetting(final String field, final String value) {
    when(this.settings.getString(field)).thenReturn(value);
    return this;
  }

  public RefChangeBuilder withToHash(final String toHash) {
    this.toHash = toHash;
    return this;
  }

  public RefChangeBuilder withType(final RefChangeType type) {
    this.type = type;
    return this;
  }

  public RefChangeBuilder withUserInBitbucket(
      final String displayName, final String email, final String name) {
    @SuppressWarnings("unchecked")
    final Page<ApplicationUser> page = mock(Page.class);
    when(page.getSize()) //
        .thenReturn(1);
    when(this.sbccUserAdminService.emailExists(email)) //
        .thenReturn(true);
    when(this.sbccUserAdminService.displayNameExists(displayName)) //
        .thenReturn(true);
    return this;
  }

  private RefChange newRefChange() {
    final RefChange refChange =
        new RefChange() {

          @Override
          public String getFromHash() {
            return RefChangeBuilder.this.fromHash;
          }

          @Override
          public MinimalRef getRef() {
            return new MinimalRef() {

              @Override
              public String getDisplayId() {
                return null;
              }

              @Override
              public String getId() {
                return RefChangeBuilder.this.refId;
              }

              @Override
              public RefType getType() {
                return null;
              }
            };
          }

          @Override
          public String getToHash() {
            return RefChangeBuilder.this.toHash;
          }

          @Override
          public RefChangeType getType() {
            return RefChangeBuilder.this.type;
          }
        };
    return refChange;
  }
}
