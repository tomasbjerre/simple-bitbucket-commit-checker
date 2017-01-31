package se.bjurr.sbcc;

import static com.atlassian.bitbucket.permission.Permission.REPO_ADMIN;
import static com.atlassian.bitbucket.user.UserType.SERVICE;
import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.SEVERE;
import static se.bjurr.sbcc.SbccPrinter.NL;
import static se.bjurr.sbcc.settings.SbccSettings.sscSettings;

import java.util.logging.Logger;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scm.pull.MergeRequest;
import com.atlassian.bitbucket.scm.pull.MergeRequestCheck;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.Operation;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.annotations.VisibleForTesting;

import se.bjurr.sbcc.commits.ChangeSetsService;
import se.bjurr.sbcc.data.SbccVerificationResult;
import se.bjurr.sbcc.settings.SbccSettings;

public class SbccRepositoryMergeRequestCheck implements MergeRequestCheck {
  public static final String PR_REJECT_DEFAULT_MSG =
      "At least one commit in Pull Request is not ok";
  private static final String HOOK_SETTINGS_KEY = "se.bjurr.sscc.sscc:pre-receive-repository-hook";
  private static Logger logger = Logger.getLogger(SbccRepositoryMergeRequestCheck.class.getName());
  private final ApplicationLinkService applicationLinkService;
  private final AuthenticationContext bitbucketAuthenticationContext;
  private ChangeSetsService changeSetService;
  private final RepositoryHookService repositoryHookService;
  private ResultsCallable resultsCallback = new ResultsCallable();
  private final SbccUserAdminService sbccUserAdminService;
  private final SecurityService securityService;

  public SbccRepositoryMergeRequestCheck(
      ChangeSetsService changeSetService,
      AuthenticationContext bitbucketAuthenticationContext,
      ApplicationLinkService applicationLinkService,
      SbccUserAdminService sbccUserAdminService,
      PluginSettingsFactory pluginSettingsFactory,
      RepositoryHookService repositoryHookService,
      SecurityService securityService) {
    this.applicationLinkService = applicationLinkService;
    this.bitbucketAuthenticationContext = bitbucketAuthenticationContext;
    this.sbccUserAdminService = sbccUserAdminService;
    this.changeSetService = changeSetService;
    this.repositoryHookService = repositoryHookService;
    this.securityService = securityService;
  }

  @Override
  public void check(MergeRequest mergeRequest) {
    try {
      Repository repository = mergeRequest.getPullRequest().getToRef().getRepository();
      Settings rawSettings = getSettings(repository, HOOK_SETTINGS_KEY);
      if (rawSettings == null) {
        logger.fine("No settings found for SBCC");
        return;
      }
      SbccRenderer sbccRenderer = new SbccRenderer(this.bitbucketAuthenticationContext);
      SbccSettings settings = sscSettings(new RenderingSettings(rawSettings, sbccRenderer));
      if (settings.isDryRun()
          || !settings.shouldCheckPullRequests()
          || settings.allowServiceUsers()
              && this.bitbucketAuthenticationContext.getCurrentUser().getType().equals(SERVICE)) {
        this.resultsCallback.report(TRUE, null, null);
        return;
      }
      PullRequest pullRequest = mergeRequest.getPullRequest();
      RefChangeValidator sbccVerificationResult =
          new RefChangeValidator(
              pullRequest.getFromRef().getRepository(),
              pullRequest.getToRef().getRepository(),
              settings,
              this.changeSetService,
              this.bitbucketAuthenticationContext,
              sbccRenderer,
              this.applicationLinkService,
              this.sbccUserAdminService);
      SbccVerificationResult refChangeVerificationResults = new SbccVerificationResult();
      sbccVerificationResult.validateRefChange(refChangeVerificationResults, pullRequest);
      boolean isAccepted = refChangeVerificationResults.isAccepted();
      if (isAccepted) {
        this.resultsCallback.report(isAccepted, null, null);
        return;
      }
      String printOut =
          new SbccPrinter(settings, sbccRenderer)
              .printVerificationResults(refChangeVerificationResults)
              .replaceAll("<", "&lt;")
              .replaceAll(">", "&gt;")
              .replaceAll(NL, "<br>\n");
      String summary = settings.getShouldCheckPullRequestsMessage().or(PR_REJECT_DEFAULT_MSG);
      mergeRequest.veto(summary, printOut);
      this.resultsCallback.report(isAccepted, summary, printOut);
    } catch (Exception e) {
      logger.log(SEVERE, "", e);
    }
  }

  public void setChangesetsService(ChangeSetsService changeSetService) {
    this.changeSetService = changeSetService;
  }

  @VisibleForTesting
  public void setResultsCallback(ResultsCallable resultsCallback) {
    this.resultsCallback = resultsCallback;
  }

  private Settings getSettings(final Repository repository, final String settingsKey)
      throws Exception {
    return this.securityService
        .withPermission(REPO_ADMIN, "Retrieving settings")
        .call(
            new Operation<Settings, Exception>() {
              @Override
              public Settings perform() throws Exception {
                return SbccRepositoryMergeRequestCheck.this.repositoryHookService.getSettings(
                    repository, settingsKey);
              }
            });
  }
}
