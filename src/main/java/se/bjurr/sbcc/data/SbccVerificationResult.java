package se.bjurr.sbcc.data;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.List;

public class SbccVerificationResult {
 private final List<SbccRefChangeVerificationResult> refChanges = newArrayList();

 public void add(SbccRefChangeVerificationResult refChangeVerificationResults) {
  refChanges.add(refChangeVerificationResults);
 }

 public List<SbccRefChangeVerificationResult> getRefChanges() {
  return refChanges;
 }

 public boolean isAccepted() {
  for (final SbccRefChangeVerificationResult c : refChanges) {
   if (c.hasErrors()) {
    return FALSE;
   }
  }
  return TRUE;
 }
}
