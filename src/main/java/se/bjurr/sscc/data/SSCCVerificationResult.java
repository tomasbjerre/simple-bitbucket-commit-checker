package se.bjurr.sscc.data;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.List;

public class SSCCVerificationResult {
 private final List<SSCCRefChangeVerificationResult> refChanges = newArrayList();

 public void add(SSCCRefChangeVerificationResult refChangeVerificationResults) {
  refChanges.add(refChangeVerificationResults);
 }

 public List<SSCCRefChangeVerificationResult> getRefChanges() {
  return refChanges;
 }

 public boolean isAccepted() {
  for (final SSCCRefChangeVerificationResult c : refChanges) {
   if (!c.isBranchNameValid()) {
    return FALSE;
   }
   for (SSCCChangeSet ssccChangeSet : c.getSsccChangeSets().keySet()) {
    if (!c.getSsccChangeSets().get(ssccChangeSet).getEmailAuthorResult()) {
     return FALSE;
    }
    if (!c.getSsccChangeSets().get(ssccChangeSet).getNameResult()) {
     return FALSE;
    }
    if (c.getSsccChangeSets().get(ssccChangeSet).hasErrors()) {
     return FALSE;
    }
   }
  }
  return TRUE;
 }
}
