package se.bjurr.sscc;

import static com.atlassian.stash.repository.RefChangeType.ADD;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.settings.SSCCSettings.sscSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.sscc.data.SSCCVerificationResult;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.stash.hook.repository.RepositoryMergeRequestCheck;
import com.atlassian.stash.hook.repository.RepositoryMergeRequestCheckContext;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.google.common.annotations.VisibleForTesting;

public class SsccRepositoryMergeRequestCheck implements RepositoryMergeRequestCheck {
 public static final String PR_REJECT_DEFAULT_MSG = "At least one commit in Pull Request is not ok";
 private static Logger logger = LoggerFactory.getLogger(SsccRepositoryMergeRequestCheck.class);
 private final StashAuthenticationContext stashAuthenticationContext;
 private final ApplicationLinkService applicationLinkService;
 private ChangeSetsService changeSetService;
 private final SsccUserAdminService ssccUserAdminService;
 private ResultsCallable resultsCallback = new ResultsCallable();

 @VisibleForTesting
 public void setResultsCallback(ResultsCallable resultsCallback) {
  this.resultsCallback = resultsCallback;
 }

 public SsccRepositoryMergeRequestCheck(ChangeSetsService changeSetService,
   StashAuthenticationContext stashAuthenticationContext, ApplicationLinkService applicationLinkService,
   SsccUserAdminService ssccUserAdminService) {
  this.applicationLinkService = applicationLinkService;
  this.stashAuthenticationContext = stashAuthenticationContext;
  this.ssccUserAdminService = ssccUserAdminService;
  this.changeSetService = changeSetService;
 }

 @Override
 public void check(RepositoryMergeRequestCheckContext mergeRequestContext) {
  try {
   final SSCCSettings settings = sscSettings(mergeRequestContext.getSettings());
   if (settings.isDryRun() || !settings.shouldCheckPullRequests()) {
    resultsCallback.report(TRUE, null, null);
    return;
   }
   PullRequest pullRequest = mergeRequestContext.getMergeRequest().getPullRequest();
   SSCCRenderer ssccRenderer = new SSCCRenderer(this.stashAuthenticationContext);
   String refId = pullRequest.getFromRef().getId();
   @SuppressWarnings("deprecation")
   String fromHash = pullRequest.getFromRef().getLatestChangeset();
   @SuppressWarnings("deprecation")
   String toHash = pullRequest.getToRef().getLatestChangeset();
   RefChangeValidator ssccVerificationResult = new RefChangeValidator(pullRequest.getFromRef().getRepository(),
     settings, changeSetService, stashAuthenticationContext, ssccRenderer, applicationLinkService, ssccUserAdminService);
   SSCCVerificationResult refChangeVerificationResults = new SSCCVerificationResult();
   ssccVerificationResult.validateRefChange(refChangeVerificationResults, ADD, refId, fromHash, toHash);
   boolean isAccepted = refChangeVerificationResults.isAccepted();
   if (isAccepted) {
    resultsCallback.report(isAccepted, null, null);
    return;
   }
   String printOut = new SSCCPrinter(settings, ssccRenderer).printVerificationResults(refChangeVerificationResults);
   String summary = settings.getShouldCheckPullRequestsMessage().or(PR_REJECT_DEFAULT_MSG);
   mergeRequestContext.getMergeRequest().veto(summary, printOut);
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
