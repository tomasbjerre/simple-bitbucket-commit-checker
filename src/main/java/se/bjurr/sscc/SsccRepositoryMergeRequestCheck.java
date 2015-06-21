package se.bjurr.sscc;

import static com.atlassian.stash.repository.RefChangeType.ADD;
import static com.atlassian.stash.user.UserType.SERVICE;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.settings.SSCCSettings.sscSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.sscc.data.SSCCVerificationResult;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.scm.pull.MergeRequest;
import com.atlassian.stash.scm.pull.MergeRequestCheck;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.google.common.annotations.VisibleForTesting;

public class SsccRepositoryMergeRequestCheck implements MergeRequestCheck {
 public static final String PR_REJECT_DEFAULT_MSG = "At least one commit in Pull Request is not ok";
 private static Logger logger = LoggerFactory.getLogger(SsccRepositoryMergeRequestCheck.class);
 private final StashAuthenticationContext stashAuthenticationContext;
 private final ApplicationLinkService applicationLinkService;
 private ChangeSetsService changeSetService;
 private final SsccUserAdminService ssccUserAdminService;
 private ResultsCallable resultsCallback = new ResultsCallable();
 private final RepositoryHookService repositoryHookService;

 @VisibleForTesting
 public void setResultsCallback(ResultsCallable resultsCallback) {
  this.resultsCallback = resultsCallback;
 }

 public SsccRepositoryMergeRequestCheck(ChangeSetsService changeSetService,
   StashAuthenticationContext stashAuthenticationContext, ApplicationLinkService applicationLinkService,
   SsccUserAdminService ssccUserAdminService, PluginSettingsFactory pluginSettingsFactory,
   RepositoryHookService repositoryHookService) {
  this.applicationLinkService = applicationLinkService;
  this.stashAuthenticationContext = stashAuthenticationContext;
  this.ssccUserAdminService = ssccUserAdminService;
  this.changeSetService = changeSetService;
  this.repositoryHookService = repositoryHookService;
 }

 @Override
 public void check(MergeRequest mergeRequest) {
  try {
   SSCCSettings settings = sscSettings(repositoryHookService.getSettings(mergeRequest.getPullRequest().getToRef()
     .getRepository(), "se.bjurr.sscc.sscc:pre-receive-repository-hook"));
   if (settings.isDryRun() || !settings.shouldCheckPullRequests() || settings.allowServiceUsers()
     && stashAuthenticationContext.getCurrentUser().getType().equals(SERVICE)) {
    resultsCallback.report(TRUE, null, null);
    return;
   }
   PullRequest pullRequest = mergeRequest.getPullRequest();
   SSCCRenderer ssccRenderer = new SSCCRenderer(this.stashAuthenticationContext);
   String refId = pullRequest.getFromRef().getId();
   @SuppressWarnings("deprecation")
   String fromHash = pullRequest.getFromRef().getLatestChangeset();
   @SuppressWarnings("deprecation")
   String toHash = pullRequest.getToRef().getLatestChangeset();
   RefChangeValidator ssccVerificationResult = new RefChangeValidator(pullRequest.getFromRef().getRepository(),
     pullRequest.getToRef().getRepository(), settings, changeSetService, stashAuthenticationContext, ssccRenderer,
     applicationLinkService, ssccUserAdminService);
   SSCCVerificationResult refChangeVerificationResults = new SSCCVerificationResult();
   ssccVerificationResult.validateRefChange(refChangeVerificationResults, ADD, refId, fromHash, toHash);
   boolean isAccepted = refChangeVerificationResults.isAccepted();
   if (isAccepted) {
    resultsCallback.report(isAccepted, null, null);
    return;
   }
   String printOut = new SSCCPrinter(settings, ssccRenderer).printVerificationResults(refChangeVerificationResults);
   String summary = settings.getShouldCheckPullRequestsMessage().or(PR_REJECT_DEFAULT_MSG);
   mergeRequest.veto(summary, printOut);
   /**
    * mergeRequestContext.getMergeRequest().getMessage() was added in Stash
    * 3.7.0, implementing it will make plugin incompatible with older
    * installations
    */
   resultsCallback.report(isAccepted, summary, printOut);
  } catch (Exception e) {
   logger.error("", e);
  }
 }

 public void setChangesetsService(ChangeSetsService changeSetService) {
  this.changeSetService = changeSetService;
 }
}
