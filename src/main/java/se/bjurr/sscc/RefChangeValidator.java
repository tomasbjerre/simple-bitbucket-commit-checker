package se.bjurr.sscc;

import static com.atlassian.stash.repository.RefChangeType.DELETE;
import static java.util.regex.Pattern.compile;
import static se.bjurr.sscc.SSCCCommon.getStashEmail;
import static se.bjurr.sscc.SSCCCommon.getStashName;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.data.SSCCRefChangeVerificationResult;
import se.bjurr.sscc.data.SSCCVerificationResult;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.user.StashAuthenticationContext;

public class RefChangeValidator {
 private static Logger logger = LoggerFactory.getLogger(RefChangeValidator.class);

 private final RepositoryHookContext repositoryHookContext;
 private final Collection<RefChange> refChanges;
 private final SSCCSettings settings;
 private final HookResponse hookResponse;
 private final ChangeSetsService changesetsService;
 private final StashAuthenticationContext stashAuthenticationContext;
 private final CommitMessageValidator commitMessageValidator;
 private final CommitContentValidator commitContentValidator;

 public RefChangeValidator(RepositoryHookContext repositoryHookContext, Collection<RefChange> refChanges,
   SSCCSettings settings, HookResponse hookResponse, ChangeSetsService changesetsService,
   StashAuthenticationContext stashAuthenticationContext) {
  this.repositoryHookContext = repositoryHookContext;
  this.refChanges = refChanges;
  this.settings = settings;
  this.hookResponse = hookResponse;
  this.changesetsService = changesetsService;
  this.stashAuthenticationContext = stashAuthenticationContext;
  this.commitMessageValidator = new CommitMessageValidator(stashAuthenticationContext);
  this.commitContentValidator = new CommitContentValidator(settings);
 }

 public SSCCVerificationResult validateRefChanges() throws IOException {
  final SSCCVerificationResult refChangeVerificationResult = new SSCCVerificationResult();
  for (final RefChange refChange : refChanges) {
   logger.info(getStashName(stashAuthenticationContext) + " " + getStashEmail(stashAuthenticationContext)
     + "> RefChange " + refChange.getFromHash() + " " + refChange.getRefId() + " " + refChange.getToHash() + " "
     + refChange.getType());
   if (compile(settings.getBranches().or(".*")).matcher(refChange.getRefId()).find()) {
    if (refChange.getType() != DELETE) {
     List<SSCCChangeSet> refChangeSets = changesetsService.getNewChangeSets(settings,
       repositoryHookContext.getRepository(), refChange);
     final SSCCRefChangeVerificationResult refChangeVerificationResults = validateRefChange(refChange, refChangeSets,
       settings, hookResponse);
     if (refChangeVerificationResults.hasReportables()) {
      refChangeVerificationResult.add(refChangeVerificationResults);
     }
    }
   }
  }
  return refChangeVerificationResult;
 }

 private SSCCRefChangeVerificationResult validateRefChange(RefChange refChange, List<SSCCChangeSet> ssccChangeSets,
   SSCCSettings settings, HookResponse hookResponse) throws IOException {
  final SSCCRefChangeVerificationResult refChangeVerificationResult = new SSCCRefChangeVerificationResult(refChange);
  for (final SSCCChangeSet ssccChangeSet : ssccChangeSets) {
   logger.info(getStashName(stashAuthenticationContext) + " " + getStashEmail(stashAuthenticationContext)
     + "> ChangeSet " + ssccChangeSet.getId() + " " + ssccChangeSet.getMessage() + " " + ssccChangeSet.getParentCount()
     + " " + ssccChangeSet.getCommitter().getEmailAddress() + " " + ssccChangeSet.getCommitter().getName());
   refChangeVerificationResult.setGroupsResult(ssccChangeSet,
     commitMessageValidator.validateChangeSetForGroups(settings, ssccChangeSet));
   refChangeVerificationResult.addEmailValidationResult(ssccChangeSet,
     commitMessageValidator.validateChangeSetForEmail(settings, ssccChangeSet));
   refChangeVerificationResult.addNameValidationResult(ssccChangeSet,
     commitMessageValidator.validateChangeSetForName(settings, ssccChangeSet));
   refChangeVerificationResult.addContentSizeValidationResult(ssccChangeSet,
     commitContentValidator.validateChangeSetForContentSize(ssccChangeSet));
   refChangeVerificationResult.addContentDiffValidationResult(ssccChangeSet,
     commitContentValidator.validateChangeSetForContentDiff(ssccChangeSet));
   refChangeVerificationResult.setBranchValidationResult(validateBranchName(refChange.getRefId()));
  }
  return refChangeVerificationResult;
 }

 private boolean validateBranchName(String branchName) {
  return compile(settings.getBranchRejectionRegexp().or(".*")).matcher(branchName).find();
 }
}
