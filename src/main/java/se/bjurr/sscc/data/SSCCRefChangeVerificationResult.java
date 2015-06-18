package se.bjurr.sscc.data;

import static com.google.common.collect.Maps.newTreeMap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.List;
import java.util.Map;

import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCMatch;

import com.google.common.base.Optional;

public class SSCCRefChangeVerificationResult {
 private final Map<SSCCChangeSet, SSCCChangeSetVerificationResult> ssccChangeSets = newTreeMap();
 private boolean branchNameValid = TRUE;
 private final String refId;
 private final String toHash;
 private final String fromHash;

 public SSCCRefChangeVerificationResult(String refId, String fromHash, String toHash) {
  this.refId = refId;
  this.fromHash = fromHash;
  this.toHash = toHash;
 }

 public void addAuthorEmailValidationResult(SSCCChangeSet ssccChangeSet, boolean validateChangeSetForAuthorEmail) {
  getOrAdd(ssccChangeSet).setEmailAuthorResult(validateChangeSetForAuthorEmail);
 }

 public void addCommitterEmailValidationResult(SSCCChangeSet ssccChangeSet, boolean validateChangeSetForCommitterEmail) {
  getOrAdd(ssccChangeSet).setEmailCommitterResult(validateChangeSetForCommitterEmail);
 }

 public void addAuthorNameValidationResult(SSCCChangeSet ssccChangeSet, boolean validateChangeSetForName) {
  getOrAdd(ssccChangeSet).setNameAuthorResult(validateChangeSetForName);
 }

 public void addCommitterNameValidationResult(SSCCChangeSet ssccChangeSet, boolean validateChangeSetForName) {
  getOrAdd(ssccChangeSet).setNameCommitterResult(validateChangeSetForName);
 }

 private SSCCChangeSetVerificationResult getOrAdd(SSCCChangeSet ssccChangeSet) {
  if (!ssccChangeSets.containsKey(ssccChangeSet)) {
   ssccChangeSets.put(ssccChangeSet, new SSCCChangeSetVerificationResult());
  }
  return ssccChangeSets.get(ssccChangeSet);
 }

 public Map<SSCCChangeSet, SSCCChangeSetVerificationResult> getSsccChangeSets() {
  return ssccChangeSets;
 }

 public boolean hasReportables() {
  if (hasErrors()) {
   return TRUE;
  }
  for (final SSCCChangeSet ssccChangeSet : ssccChangeSets.keySet()) {
   if (ssccChangeSets.get(ssccChangeSet).hasReportables()) {
    return TRUE;
   }
  }
  return FALSE;
 }

 public boolean hasErrors() {
  if (!branchNameValid) {
   return TRUE;
  }
  for (final SSCCChangeSet ssccChangeSet : ssccChangeSets.keySet()) {
   if (ssccChangeSets.get(ssccChangeSet).hasErrors()) {
    return TRUE;
   }
  }
  return FALSE;
 }

 public void setGroupsResult(SSCCChangeSet ssccChangeSet, Map<SSCCGroup, SSCCMatch> groupsResult) {
  getOrAdd(ssccChangeSet).setGroupsResult(groupsResult);
 }

 public void addContentSizeValidationResult(SSCCChangeSet ssccChangeSet,
   Map<String, Long> validateChangeSetForContentSize) {
  getOrAdd(ssccChangeSet).addContentSizeValidationResult(validateChangeSetForContentSize);
 }

 public void addContentDiffValidationResult(SSCCChangeSet ssccChangeSet,
   Optional<String> validateChangeSetForContentDiff) {
  getOrAdd(ssccChangeSet).addContentDiffValidationResult(validateChangeSetForContentDiff);
 }

 public void setFailingJql(SSCCChangeSet ssccChangeSet, List<String> failingJqlQueries) {
  getOrAdd(ssccChangeSet).setFailingJql(failingJqlQueries);
 }

 public boolean isBranchNameValid() {
  return branchNameValid;
 }

 public void setBranchValidationResult(boolean branchNameValid) {
  this.branchNameValid = branchNameValid;
 }

 public void addAuthorEmailInStashValidationResult(SSCCChangeSet ssccChangeSet,
   boolean validateChangeSetForAuthorEmailInStash) {
  getOrAdd(ssccChangeSet).addAuthorEmailInStashValidationResult(validateChangeSetForAuthorEmailInStash);
 }

 public void addAuthorNameInStashValidationResult(SSCCChangeSet ssccChangeSet,
   boolean validateChangeSetForAuthorNameInStash) {
  getOrAdd(ssccChangeSet).addAuthorNameInStashValidationResult(validateChangeSetForAuthorNameInStash);
 }

 public String getRefId() {
  return refId;
 }

 public String getToHash() {
  return toHash;
 }

 public String getFromHash() {
  return fromHash;
 }
}