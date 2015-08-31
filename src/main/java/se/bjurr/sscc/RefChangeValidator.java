package se.bjurr.sscc;

import static com.atlassian.stash.repository.RefChangeType.DELETE;
import static java.util.regex.Pattern.compile;
import static se.bjurr.sscc.SSCCCommon.getStashEmail;
import static se.bjurr.sscc.SSCCCommon.getStashName;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.data.SSCCRefChangeVerificationResult;
import se.bjurr.sscc.data.SSCCVerificationResult;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.StashAuthenticationContext;

public class RefChangeValidator {
 private static Logger logger = Logger.getLogger(RefChangeValidator.class.getName());

 private final SSCCSettings settings;
 private final ChangeSetsService changesetsService;
 private final StashAuthenticationContext stashAuthenticationContext;
 private final CommitMessageValidator commitMessageValidator;
 private final CommitContentValidator commitContentValidator;

 private final SSCCRenderer ssccRenderer;

 private final JqlValidator jqlValidator;

 private final Repository fromRepository;

 public RefChangeValidator(Repository fromRepository, Repository toRepository, SSCCSettings settings,
   ChangeSetsService changesetsService, StashAuthenticationContext stashAuthenticationContext,
   SSCCRenderer ssccRenderer, ApplicationLinkService applicationLinkService, SsccUserAdminService ssccUserAdminService) {
  this.fromRepository = fromRepository;
  this.settings = settings;
  this.changesetsService = changesetsService;
  this.stashAuthenticationContext = stashAuthenticationContext;
  this.commitMessageValidator = new CommitMessageValidator(stashAuthenticationContext, ssccUserAdminService);
  this.commitContentValidator = new CommitContentValidator(settings);
  this.ssccRenderer = ssccRenderer;
  this.jqlValidator = new JqlValidator(applicationLinkService, settings, ssccRenderer);
 }

 public SSCCVerificationResult validateRefChanges(Collection<RefChange> refChanges) throws IOException,
   CredentialsRequiredException, ResponseException, ExecutionException {
  final SSCCVerificationResult refChangeVerificationResult = new SSCCVerificationResult();
  for (final RefChange refChange : refChanges) {
   validateRefChange(refChangeVerificationResult, refChange.getType(), refChange.getRefId(), refChange.getFromHash(),
     refChange.getToHash());
  }
  return refChangeVerificationResult;
 }

 public void validateRefChange(final SSCCVerificationResult refChangeVerificationResult, RefChangeType refChangeType,
   String refId, String fromHash, String toHash) throws IOException, CredentialsRequiredException, ResponseException,
   ExecutionException {
  logger.fine(getStashName(stashAuthenticationContext) + " " + getStashEmail(stashAuthenticationContext)
    + "> RefChange " + fromHash + " " + refId + " " + toHash + " " + refChangeType);
  if (compile(settings.getBranches().or(".*")).matcher(refId).find()) {
   if (refChangeType != DELETE) {
    List<SSCCChangeSet> refChangeSets = changesetsService.getNewChangeSets(settings, fromRepository, refId,
      refChangeType, fromHash, toHash);
    validateRefChange(refChangeVerificationResult, refId, fromHash, toHash, refChangeSets);
   }
  }
 }

 public void validateRefChange(SSCCVerificationResult refChangeVerificationResults, PullRequest pullRequest)
   throws IOException, CredentialsRequiredException, ResponseException, ExecutionException {
  String refId = pullRequest.getFromRef().getId();
  @SuppressWarnings("deprecation")
  String fromHash = pullRequest.getFromRef().getLatestChangeset();
  @SuppressWarnings("deprecation")
  String toHash = pullRequest.getToRef().getLatestChangeset();
  List<SSCCChangeSet> refChangeSets = changesetsService.getNewChangeSets(settings, pullRequest);
  validateRefChange(refChangeVerificationResults, refId, fromHash, toHash, refChangeSets);
 }

 private void validateRefChange(final SSCCVerificationResult refChangeVerificationResult, String refId,
   String fromHash, String toHash, List<SSCCChangeSet> refChangeSets) throws IOException, CredentialsRequiredException,
   ResponseException, ExecutionException {
  SSCCRefChangeVerificationResult refChangeVerificationResults = validateRefChange(refChangeSets, settings, refId,
    fromHash, toHash);
  if (refChangeVerificationResults.hasReportables()) {
   refChangeVerificationResult.add(refChangeVerificationResults);
  }
 }

 private SSCCRefChangeVerificationResult validateRefChange(List<SSCCChangeSet> ssccChangeSets, SSCCSettings settings,
   String refId, String fromHash, String toHash) throws IOException, CredentialsRequiredException, ResponseException,
   ExecutionException {
  final SSCCRefChangeVerificationResult refChangeVerificationResult = new SSCCRefChangeVerificationResult(refId,
    fromHash, toHash);
  refChangeVerificationResult.setBranchValidationResult(validateBranchName(refId));
  for (final SSCCChangeSet ssccChangeSet : ssccChangeSets) {
   ssccRenderer.setSsccChangeSet(ssccChangeSet);
   logger.fine(getStashName(stashAuthenticationContext) + " " + getStashEmail(stashAuthenticationContext)
     + "> ChangeSet " + ssccChangeSet.getId() + " " + ssccChangeSet.getMessage() + " " + ssccChangeSet.getParentCount()
     + " " + ssccChangeSet.getCommitter().getEmailAddress() + " " + ssccChangeSet.getCommitter().getName());
   refChangeVerificationResult.setGroupsResult(ssccChangeSet,
     commitMessageValidator.validateChangeSetForGroups(settings, ssccChangeSet));
   refChangeVerificationResult.addAuthorEmailValidationResult(ssccChangeSet,
     commitMessageValidator.validateChangeSetForAuthorEmail(settings, ssccChangeSet, ssccRenderer));
   refChangeVerificationResult.addCommitterEmailValidationResult(ssccChangeSet,
     commitMessageValidator.validateChangeSetForCommitterEmail(settings, ssccChangeSet, ssccRenderer));
   refChangeVerificationResult.addAuthorNameValidationResult(ssccChangeSet,
     commitMessageValidator.validateChangeSetForAuthorName(settings, ssccChangeSet));
   refChangeVerificationResult.addCommitterNameValidationResult(ssccChangeSet,
     commitMessageValidator.validateChangeSetForCommitterName(settings, ssccChangeSet));
   refChangeVerificationResult.addContentSizeValidationResult(ssccChangeSet,
     commitContentValidator.validateChangeSetForContentSize(ssccChangeSet));
   refChangeVerificationResult.addContentDiffValidationResult(ssccChangeSet,
     commitContentValidator.validateChangeSetForContentDiff(ssccChangeSet));
   refChangeVerificationResult.addAuthorEmailInStashValidationResult(ssccChangeSet,
     commitMessageValidator.validateChangeSetForAuthorEmailInStash(settings, ssccChangeSet));
   refChangeVerificationResult.addAuthorNameInStashValidationResult(ssccChangeSet,
     commitMessageValidator.validateChangeSetForAuthorNameInStash(settings, ssccChangeSet));

   refChangeVerificationResult.setFailingJql(ssccChangeSet, jqlValidator.validateJql(ssccChangeSet));
   ssccRenderer.setSsccChangeSet(null);
  }
  return refChangeVerificationResult;
 }

 private boolean validateBranchName(String branchName) {
  return compile(settings.getBranchRejectionRegexp().or(".*")).matcher(branchName).find();
 }
}
