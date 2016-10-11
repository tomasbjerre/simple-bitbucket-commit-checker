package se.bjurr.sbcc.data;

import static com.google.common.collect.Maps.newTreeMap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.List;
import java.util.Map;

import se.bjurr.sbcc.settings.SbccGroup;
import se.bjurr.sbcc.settings.SbccMatch;

import com.google.common.base.Optional;

public class SbccRefChangeVerificationResult {
 private final Map<SbccChangeSet, SbccChangeSetVerificationResult> sbccChangeSets = newTreeMap();
 private boolean branchNameValid = TRUE;
 private final String refId;
 private final String toHash;
 private final String fromHash;

 public SbccRefChangeVerificationResult(String refId, String fromHash, String toHash) {
  this.refId = refId;
  this.fromHash = fromHash;
  this.toHash = toHash;
 }

 public void addAuthorEmailValidationResult(SbccChangeSet sbccChangeSet, boolean validateChangeSetForAuthorEmail) {
  getOrAdd(sbccChangeSet).setEmailAuthorResult(validateChangeSetForAuthorEmail);
 }

 public void addCommitterEmailValidationResult(SbccChangeSet sbccChangeSet,
   boolean validateChangeSetForCommitterEmail) {
  getOrAdd(sbccChangeSet).setEmailCommitterResult(validateChangeSetForCommitterEmail);
 }

 public void addAuthorNameValidationResult(SbccChangeSet sbccChangeSet, boolean validateChangeSetForName) {
  getOrAdd(sbccChangeSet).setNameAuthorResult(validateChangeSetForName);
 }

 public void addCommitterNameValidationResult(SbccChangeSet sbccChangeSet, boolean validateChangeSetForName) {
  getOrAdd(sbccChangeSet).setNameCommitterResult(validateChangeSetForName);
 }

 private SbccChangeSetVerificationResult getOrAdd(SbccChangeSet sbccChangeSet) {
  if (!sbccChangeSets.containsKey(sbccChangeSet)) {
   sbccChangeSets.put(sbccChangeSet, new SbccChangeSetVerificationResult());
  }
  return sbccChangeSets.get(sbccChangeSet);
 }

 public Map<SbccChangeSet, SbccChangeSetVerificationResult> getSbccChangeSets() {
  return sbccChangeSets;
 }

 public boolean hasReportables() {
  if (hasErrors()) {
   return TRUE;
  }
  for (final SbccChangeSet sbccChangeSet : sbccChangeSets.keySet()) {
   if (sbccChangeSets.get(sbccChangeSet).hasReportables()) {
    return TRUE;
   }
  }
  return FALSE;
 }

 public boolean hasErrors() {
  if (!branchNameValid) {
   return TRUE;
  }
  for (final SbccChangeSet sbccChangeSet : sbccChangeSets.keySet()) {
   if (sbccChangeSets.get(sbccChangeSet).hasErrors()) {
    return TRUE;
   }
  }
  return FALSE;
 }

 public void setGroupsResult(SbccChangeSet sbccChangeSet, Map<SbccGroup, SbccMatch> groupsResult) {
  getOrAdd(sbccChangeSet).setGroupsResult(groupsResult);
 }

 public void addContentSizeValidationResult(SbccChangeSet sbccChangeSet,
   Map<String, Long> validateChangeSetForContentSize) {
  getOrAdd(sbccChangeSet).addContentSizeValidationResult(validateChangeSetForContentSize);
 }

 public void addContentDiffValidationResult(SbccChangeSet sbccChangeSet,
   Optional<String> validateChangeSetForContentDiff) {
  getOrAdd(sbccChangeSet).addContentDiffValidationResult(validateChangeSetForContentDiff);
 }

 public void setFailingJql(SbccChangeSet sbccChangeSet, List<String> failingJqlQueries) {
  getOrAdd(sbccChangeSet).setFailingJql(failingJqlQueries);
 }

 public boolean isBranchNameValid() {
  return branchNameValid;
 }

 public void setBranchValidationResult(boolean branchNameValid) {
  this.branchNameValid = branchNameValid;
 }

 public void addAuthorEmailInBitbucketValidationResult(SbccChangeSet sbccChangeSet,
   boolean validateChangeSetForAuthorEmailInBitbucket) {
  getOrAdd(sbccChangeSet).addAuthorEmailInBitbucketValidationResult(validateChangeSetForAuthorEmailInBitbucket);
 }

 public void addAuthorNameInBitbucketValidationResult(SbccChangeSet sbccChangeSet,
   boolean validateChangeSetForAuthorNameInBitbucket) {
  getOrAdd(sbccChangeSet).addAuthorNameInBitbucketValidationResult(validateChangeSetForAuthorNameInBitbucket);
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