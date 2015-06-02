package se.bjurr.sscc;

import static se.bjurr.sscc.settings.SSCCGroup.Accept.ACCEPT;
import static se.bjurr.sscc.settings.SSCCGroup.Accept.SHOW_MESSAGE;

import java.util.Collection;
import java.util.Map;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.data.SSCCChangeSetVerificationResult;
import se.bjurr.sscc.data.SSCCRefChangeVerificationResult;
import se.bjurr.sscc.data.SSCCVerificationResult;
import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCRule;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.stash.repository.RefChange;
import com.google.common.base.Optional;

public class SSCCPrinter {

 private final SSCCSettings settings;
 private final SSCCRenderer ssccRenderer;

 public SSCCPrinter(SSCCSettings settings, SSCCRenderer ssccRenderer) {
  this.settings = settings;
  this.ssccRenderer = ssccRenderer;
 }

 private void printAcceptMessages(final SSCCGroup ssccVerificationResult) {
  if (ssccVerificationResult.getAccept().equals(ACCEPT)) {
   ssccRenderer.println();
   if (ssccVerificationResult.getMessage().isPresent()) {
    ssccRenderer.println("- " + ssccVerificationResult.getMessage().get());
   }
   for (final SSCCRule ssccRule : ssccVerificationResult.getRules()) {
    if (ssccRule.getMessage().isPresent()) {
     ssccRenderer.println("  " + ssccRule.getMessage().get() + ": " + ssccRule.getRegexp());
    }
   }
  }
 }

 private void printCommit(final SSCCChangeSet ssccChangeSet) {
  ssccRenderer.println();
  ssccRenderer.println();
  ssccRenderer.println(ssccChangeSet.getId() + " " + ssccChangeSet.getCommitter().getName() + " <"
    + ssccChangeSet.getCommitter().getEmailAddress() + ">");
  ssccRenderer.println(">>> " + ssccChangeSet.getMessage());
 }

 private void printEmailVerification(SSCCSettings settings,
   final SSCCRefChangeVerificationResult refChangeVerificationResult, final SSCCChangeSet ssccChangeSet) {
  if (!refChangeVerificationResult.getSsccChangeSets().get(ssccChangeSet).getEmailResult()) {
   ssccRenderer.println();
   ssccRenderer.println("- Stash: '${" + SSCCRenderer.SSCCVariable.STASH_EMAIL + "}' != Commit: '"
     + ssccChangeSet.getCommitter().getEmailAddress() + "'");
   if (settings.getRequireMatchingAuthorEmailMessage().isPresent()) {
    ssccRenderer.println("  " + settings.getRequireMatchingAuthorEmailMessage().get());
   }
  }
 }

 private void printBranchNameVerification(SSCCSettings settings,
   SSCCRefChangeVerificationResult refChangeVerificationResult) {
  if (!refChangeVerificationResult.isBranchNameValid()) {
   ssccRenderer.println();
   ssccRenderer.println("- Branch: " + refChangeVerificationResult.getRefChange().getRefId() + ", "
     + settings.getBranchRejectionRegexp().get());
   if (settings.getBranchRejectionRegexpMessage().isPresent()) {
    ssccRenderer.println("  " + settings.getBranchRejectionRegexpMessage().get());
   }
  }
 }

 private void printNameVerification(SSCCSettings settings,
   final SSCCRefChangeVerificationResult refChangeVerificationResult, final SSCCChangeSet ssccChangeSet) {
  if (!refChangeVerificationResult.getSsccChangeSets().get(ssccChangeSet).getNameResult()) {
   ssccRenderer.println();
   ssccRenderer.println("- Stash: '${" + SSCCRenderer.SSCCVariable.STASH_NAME + "}' != Commit: '"
     + ssccChangeSet.getCommitter().getName() + "'");
   if (settings.getRequireMatchingAuthorNameMessage().isPresent()) {
    ssccRenderer.println("  " + settings.getRequireMatchingAuthorNameMessage().get());
   }
  }
 }

 private void printRejectedContent(Optional<String> rejectedContent) {
  if (rejectedContent.isPresent()) {
   ssccRenderer.println();
   ssccRenderer.println("- " + settings.getCommitDiffRegexp().get() + ":");
   ssccRenderer.println(rejectedContent.get().replaceAll("$", "\n  "));
   if (settings.getCommitDiffRegexpMessage().isPresent()) {
    ssccRenderer.println("  " + settings.getCommitDiffRegexpMessage().get());
   }
  }
 }

 private void printMaximumSizeExceeded(Map<String, Long> map) {
  for (String file : map.keySet()) {
   Long sizeKb = map.get(file);
   ssccRenderer.println();
   ssccRenderer.println("- " + file + " " + sizeKb + "kb > " + settings.getCommitSizeKb() + "kb");
   if (settings.getCommitSizeMessage().isPresent()) {
    ssccRenderer.println("  " + settings.getCommitSizeMessage().get());
   }
  }
 }

 private void printRefChange(final RefChange refChange) {
  ssccRenderer.println(refChange.getRefId() + " " + refChange.getFromHash().substring(0, 10) + " -> "
    + refChange.getToHash().substring(0, 10));
 }

 private void printRuleMessage(final SSCCGroup ssccVerificationResult) {
  if (ssccVerificationResult.getAccept().equals(SHOW_MESSAGE)) {
   if (ssccVerificationResult.getMessage().isPresent()) {
    ssccRenderer.println();
    ssccRenderer.println("- " + ssccVerificationResult.getMessage().get());
   }
  }
 }

 public void printVerificationResults(Collection<RefChange> refChanges, SSCCVerificationResult verificationResult) {
  if (verificationResult.isAccepted()) {
   if (settings.getAcceptMessage().isPresent()) {
    ssccRenderer.println(settings.getAcceptMessage().get());
   }
  } else {
   if (settings.getRejectMessage().isPresent()) {
    ssccRenderer.println(settings.getRejectMessage().get());
   }
  }

  for (final SSCCRefChangeVerificationResult refChangeVerificationResult : verificationResult.getRefChanges()) {
   if (!refChangeVerificationResult.hasReportables()) {
    continue;
   }
   printRefChange(refChangeVerificationResult.getRefChange());
   printBranchNameVerification(settings, refChangeVerificationResult);
   for (final SSCCChangeSet ssccChangeSet : refChangeVerificationResult.getSsccChangeSets().keySet()) {
    SSCCChangeSetVerificationResult changeSetVerificationResult = refChangeVerificationResult.getSsccChangeSets().get(
      ssccChangeSet);
    if (!changeSetVerificationResult.hasReportables()) {
     continue;
    }
    printCommit(ssccChangeSet);
    printEmailVerification(settings, refChangeVerificationResult, ssccChangeSet);
    printNameVerification(settings, refChangeVerificationResult, ssccChangeSet);
    printMaximumSizeExceeded(changeSetVerificationResult.getExceeding());
    printRejectedContent(changeSetVerificationResult.getRejectedContent());
    for (final SSCCGroup ssccVerificationResult : changeSetVerificationResult.getGroupsResult().keySet()) {
     printAcceptMessages(ssccVerificationResult);
     printRuleMessage(ssccVerificationResult);
    }
   }
  }

  if (settings.getAcceptMessage().isPresent() || !verificationResult.isAccepted()) {
   ssccRenderer.println();
  }
 }
}
