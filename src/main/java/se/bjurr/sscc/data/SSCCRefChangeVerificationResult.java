package se.bjurr.sscc.data;

import static com.google.common.collect.Maps.newTreeMap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.Map;

import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCMatch;

import com.atlassian.stash.repository.RefChange;

public class SSCCRefChangeVerificationResult {
 private final RefChange refChange;
 private final Map<SSCCChangeSet, SSCCChangeSetVerificationResult> ssccChangeSets = newTreeMap();

 public SSCCRefChangeVerificationResult(RefChange refChange) {
  this.refChange = refChange;
 }

 public void addEmailValidationResult(SSCCChangeSet ssccChangeSet, boolean validateChangeSetForEmail) {
  getOrAdd(ssccChangeSet).setEmailResult(validateChangeSetForEmail);
 }

 public void addNameValidationResult(SSCCChangeSet ssccChangeSet, boolean validateChangeSetForName) {
  getOrAdd(ssccChangeSet).setNameResult(validateChangeSetForName);
 }

 private SSCCChangeSetVerificationResult getOrAdd(SSCCChangeSet ssccChangeSet) {
  if (!ssccChangeSets.containsKey(ssccChangeSet)) {
   ssccChangeSets.put(ssccChangeSet, new SSCCChangeSetVerificationResult());
  }
  return ssccChangeSets.get(ssccChangeSet);
 }

 public RefChange getRefChange() {
  return refChange;
 }

 public Map<SSCCChangeSet, SSCCChangeSetVerificationResult> getSsccChangeSets() {
  return ssccChangeSets;
 }

 public boolean hasReportables() {
  for (final SSCCChangeSet ssccChangeSet : ssccChangeSets.keySet()) {
   if (ssccChangeSets.get(ssccChangeSet).hasReportables()) {
    return TRUE;
   }
  }
  return FALSE;
 }

 public boolean isEmpty() {
  return ssccChangeSets.isEmpty();
 }

 public void setGroupsResult(SSCCChangeSet ssccChangeSet, Map<SSCCGroup, SSCCMatch> groupsResult) {
  getOrAdd(ssccChangeSet).setGroupsResult(groupsResult);
 }
}