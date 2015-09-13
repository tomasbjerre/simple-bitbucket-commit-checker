package se.bjurr.sbcc;

import static com.atlassian.bitbucket.permission.Permission.REPO_ADMIN;
import static com.atlassian.bitbucket.user.UserType.SERVICE;
import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.SEVERE;
import static se.bjurr.sbcc.SbccPrinter.NL;
import static se.bjurr.sbcc.settings.SbccSettings.sscSettings;

import java.util.logging.Logger;

import se.bjurr.sbcc.data.SbccVerificationResult;
import se.bjurr.sbcc.settings.SbccSettings;

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

public class SbccRepositoryMergeRequestCheck implements MergeRequestCheck {
 private static final String HOOK_SETTINGS_KEY = "se.bjurr.sscc.sscc:pre-receive-repository-hook";
 public static final String PR_REJECT_DEFAULT_MSG = "At least one commit in Pull Request is not ok";
 private static Logger logger = Logger.getLogger(SbccRepositoryMergeRequestCheck.class.getName());
 private final AuthenticationContext bitbucketAuthenticationContext;
 private final ApplicationLinkService applicationLinkService;
 private ChangeSetsService changeSetService;
 private final SbccUserAdminService sbccUserAdminService;
 private ResultsCallable resultsCallback = new ResultsCallable();
 private final RepositoryHookService repositoryHookService;
 private final SecurityService securityService;

 @VisibleForTesting
 public void setResultsCallback(ResultsCallable resultsCallback) {
  this.resultsCallback = resultsCallback;
 }

 public SbccRepositoryMergeRequestCheck(ChangeSetsService changeSetService,
   AuthenticationContext bitbucketAuthenticationContext, ApplicationLinkService applicationLinkService,
   SbccUserAdminService sbccUserAdminService, PluginSettingsFactory pluginSettingsFactory,
   RepositoryHookService repositoryHookService, SecurityService securityService) {
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
   SbccSettings settings = sscSettings(rawSettings);
   if (settings.isDryRun() || !settings.shouldCheckPullRequests() || settings.allowServiceUsers()
     && bitbucketAuthenticationContext.getCurrentUser().getType().equals(SERVICE)) {
    resultsCallback.report(TRUE, null, null);
    return;
   }
   PullRequest pullRequest = mergeRequest.getPullRequest();
   SbccRenderer sbccRenderer = new SbccRenderer(this.bitbucketAuthenticationContext);
   RefChangeValidator sbccVerificationResult = new RefChangeValidator(pullRequest.getFromRef().getRepository(),
     pullRequest.getToRef().getRepository(), settings, changeSetService, bitbucketAuthenticationContext, sbccRenderer,
     applicationLinkService, sbccUserAdminService);
   SbccVerificationResult refChangeVerificationResults = new SbccVerificationResult();
   sbccVerificationResult.validateRefChange(refChangeVerificationResults, pullRequest);
   boolean isAccepted = refChangeVerificationResults.isAccepted();
   if (isAccepted) {
    resultsCallback.report(isAccepted, null, null);
    return;
   }
   String printOut = new SbccPrinter(settings, sbccRenderer).printVerificationResults(refChangeVerificationResults)
     .replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll(NL, "<br>\n");
   String summary = settings.getShouldCheckPullRequestsMessage().or(PR_REJECT_DEFAULT_MSG);
   mergeRequest.veto(summary, printOut);
   resultsCallback.report(isAccepted, summary, printOut);
  } catch (Exception e) {
   logger.log(SEVERE, "", e);
  }
 }

 private Settings getSettings(final Repository repository, final String settingsKey) throws Exception {
  return securityService.withPermission(REPO_ADMIN, "Retrieving settings").call(new Operation<Settings, Exception>() {
   @Override
   public Settings perform() throws Exception {
    return repositoryHookService.getSettings(repository, settingsKey);
   }
  });
 }

 public void setChangesetsService(ChangeSetsService changeSetService) {
  this.changeSetService = changeSetService;
 }
}
