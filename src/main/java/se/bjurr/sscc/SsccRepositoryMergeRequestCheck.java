package se.bjurr.sscc;

import static com.atlassian.stash.user.Permission.REPO_ADMIN;
import static com.atlassian.stash.user.UserType.SERVICE;
import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.SEVERE;
import static se.bjurr.sscc.SSCCPrinter.NL;
import static se.bjurr.sscc.settings.SSCCSettings.sscSettings;

import java.util.logging.Logger;

import se.bjurr.sscc.data.SSCCVerificationResult;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.scm.pull.MergeRequest;
import com.atlassian.stash.scm.pull.MergeRequestCheck;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.user.SecurityService;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.util.Operation;
import com.google.common.annotations.VisibleForTesting;

public class SsccRepositoryMergeRequestCheck implements MergeRequestCheck {
 private static final String SSCC_SETTINGS_KEY = "se.bjurr.sscc.sscc:pre-receive-repository-hook";
 public static final String PR_REJECT_DEFAULT_MSG = "At least one commit in Pull Request is not ok";
 private static Logger logger = Logger.getLogger(SsccRepositoryMergeRequestCheck.class.getName());
 private final StashAuthenticationContext stashAuthenticationContext;
 private final ApplicationLinkService applicationLinkService;
 private ChangeSetsService changeSetService;
 private final SsccUserAdminService ssccUserAdminService;
 private ResultsCallable resultsCallback = new ResultsCallable();
 private final RepositoryHookService repositoryHookService;
 private final SecurityService securityService;

 @VisibleForTesting
 public void setResultsCallback(ResultsCallable resultsCallback) {
  this.resultsCallback = resultsCallback;
 }

 public SsccRepositoryMergeRequestCheck(ChangeSetsService changeSetService,
   StashAuthenticationContext stashAuthenticationContext, ApplicationLinkService applicationLinkService,
   SsccUserAdminService ssccUserAdminService, PluginSettingsFactory pluginSettingsFactory,
   RepositoryHookService repositoryHookService, SecurityService securityService) {
  this.applicationLinkService = applicationLinkService;
  this.stashAuthenticationContext = stashAuthenticationContext;
  this.ssccUserAdminService = ssccUserAdminService;
  this.changeSetService = changeSetService;
  this.repositoryHookService = repositoryHookService;
  this.securityService = securityService;
 }

 @Override
 public void check(MergeRequest mergeRequest) {
  try {
   Repository repository = mergeRequest.getPullRequest().getToRef().getRepository();
   Settings rawSettings = getSettings(repository, SSCC_SETTINGS_KEY);
   if (rawSettings == null) {
    logger.fine("No settings found for SSCC");
    return;
   }
   SSCCSettings settings = sscSettings(rawSettings);
   if (settings.isDryRun() || !settings.shouldCheckPullRequests() || settings.allowServiceUsers()
     && stashAuthenticationContext.getCurrentUser().getType().equals(SERVICE)) {
    resultsCallback.report(TRUE, null, null);
    return;
   }
   PullRequest pullRequest = mergeRequest.getPullRequest();
   SSCCRenderer ssccRenderer = new SSCCRenderer(this.stashAuthenticationContext);
   RefChangeValidator ssccVerificationResult = new RefChangeValidator(pullRequest.getFromRef().getRepository(),
     pullRequest.getToRef().getRepository(), settings, changeSetService, stashAuthenticationContext, ssccRenderer,
     applicationLinkService, ssccUserAdminService);
   SSCCVerificationResult refChangeVerificationResults = new SSCCVerificationResult();
   ssccVerificationResult.validateRefChange(refChangeVerificationResults, pullRequest);
   boolean isAccepted = refChangeVerificationResults.isAccepted();
   if (isAccepted) {
    resultsCallback.report(isAccepted, null, null);
    return;
   }
   String printOut = new SSCCPrinter(settings, ssccRenderer).printVerificationResults(refChangeVerificationResults)
     .replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll(NL, "<br>\n");
   String summary = settings.getShouldCheckPullRequestsMessage().or(PR_REJECT_DEFAULT_MSG);
   mergeRequest.veto(summary, printOut);
   /**
    * mergeRequestContext.getMergeRequest().getMessage() was added in Stash
    * 3.7.0, implementing it will make plugin incompatible with older
    * installations
    */
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
