package se.bjurr.sscc;

import static com.atlassian.stash.repository.RefChangeType.DELETE;
import static java.util.regex.Pattern.compile;
import static se.bjurr.sscc.SSCCCommon.getStashEmail;
import static se.bjurr.sscc.SSCCCommon.getStashName;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.data.SSCCRefChangeVerificationResult;
import se.bjurr.sscc.data.SSCCVerificationResult;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.StashAuthenticationContext;

public class RefChangeValidator {
 private static Logger logger = LoggerFactory.getLogger(RefChangeValidator.class);

 private final SSCCSettings settings;
 private final ChangeSetsService changesetsService;
 private final StashAuthenticationContext stashAuthenticationContext;
 private final CommitMessageValidator commitMessageValidator;
 private final CommitContentValidator commitContentValidator;

 private final SSCCRenderer ssccRenderer;

 private final JqlValidator jqlValidator;

 private final Repository repository;

 public RefChangeValidator(Repository repository, SSCCSettings settings, ChangeSetsService changesetsService,
   StashAuthenticationContext stashAuthenticationContext, SSCCRenderer ssccRenderer,
   ApplicationLinkService applicationLinkService, SsccUserAdminService ssccUserAdminService) {
  this.repository = repository;
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
  logger.info(getStashName(stashAuthenticationContext) + " " + getStashEmail(stashAuthenticationContext)
    + "> RefChange " + fromHash + " " + refId + " " + toHash + " " + refChangeType);
  if (compile(settings.getBranches().or(".*")).matcher(refId).find()) {
   if (refChangeType != DELETE) {
    List<SSCCChangeSet> refChangeSets = changesetsService.getNewChangeSets(settings, repository, refId, refChangeType,
      fromHash, toHash);
    final SSCCRefChangeVerificationResult refChangeVerificationResults = validateRefChange(refChangeSets, settings,
      refId, fromHash, toHash);
    if (refChangeVerificationResults.hasReportables()) {
     refChangeVerificationResult.add(refChangeVerificationResults);
    }
   }
  }
 }

 private SSCCRefChangeVerificationResult validateRefChange(List<SSCCChangeSet> ssccChangeSets, SSCCSettings settings,
   String refId, String fromHash, String toHash) throws IOException, CredentialsRequiredException, ResponseException,
   ExecutionException {
  final SSCCRefChangeVerificationResult refChangeVerificationResult = new SSCCRefChangeVerificationResult(refId,
    fromHash, toHash);
  for (final SSCCChangeSet ssccChangeSet : ssccChangeSets) {
   logger.info(getStashName(stashAuthenticationContext) + " " + getStashEmail(stashAuthenticationContext)
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

   refChangeVerificationResult.setBranchValidationResult(validateBranchName(refId));
   refChangeVerificationResult.setFailingJql(ssccChangeSet, jqlValidator.validateJql(ssccChangeSet));
  }
  return refChangeVerificationResult;
 }

 private boolean validateBranchName(String branchName) {
  return compile(settings.getBranchRejectionRegexp().or(".*")).matcher(branchName).find();
 }
}
