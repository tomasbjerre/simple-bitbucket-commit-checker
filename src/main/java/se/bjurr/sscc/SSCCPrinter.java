package se.bjurr.sscc;

import static se.bjurr.sscc.settings.SSCCGroup.Accept.ACCEPT;
import static se.bjurr.sscc.settings.SSCCGroup.Accept.SHOW_MESSAGE;

import java.util.Map;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.data.SSCCChangeSetVerificationResult;
import se.bjurr.sscc.data.SSCCRefChangeVerificationResult;
import se.bjurr.sscc.data.SSCCVerificationResult;
import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCRule;
import se.bjurr.sscc.settings.SSCCSettings;

import com.google.common.base.Optional;

public class SSCCPrinter {

 private final SSCCSettings settings;
 private final SSCCRenderer ssccRenderer;
 public static String NL = System.getProperty("line.separator");

 public SSCCPrinter(SSCCSettings settings, SSCCRenderer ssccRenderer) {
  this.settings = settings;
  this.ssccRenderer = ssccRenderer;
 }

 private void printAcceptMessages(final SSCCGroup ssccVerificationResult, StringBuilder sb) {
  if (ssccVerificationResult.getAccept().equals(ACCEPT)) {
   ssccRenderer.append(sb, NL);
   if (ssccVerificationResult.getMessage().isPresent()) {
    ssccRenderer.append(sb, "- " + ssccVerificationResult.getMessage().get() + NL);
   }
   for (final SSCCRule ssccRule : ssccVerificationResult.getRules()) {
    if (ssccRule.getMessage().isPresent()) {
     ssccRenderer.append(sb, "  " + ssccRule.getMessage().get() + ": " + ssccRule.getRegexp() + NL);
    }
   }
  }
 }

 private void printCommit(final SSCCChangeSet ssccChangeSet, StringBuilder sb) {
  ssccRenderer.append(sb, NL);
  ssccRenderer.append(sb, NL);
  ssccRenderer.append(sb, ssccChangeSet.getId() + " " + ssccChangeSet.getAuthor().getName() + " <"
    + ssccChangeSet.getAuthor().getEmailAddress() + ">" + NL);
  ssccRenderer.append(sb, ">>> " + ssccChangeSet.getMessage() + NL);
 }

 private void printEmailVerification(final SSCCRefChangeVerificationResult refChangeVerificationResult,
   final SSCCChangeSet ssccChangeSet, StringBuilder sb) {
  if (!refChangeVerificationResult.getSsccChangeSets().get(ssccChangeSet).getEmailAuthorResult()) {
   ssccRenderer.append(sb, NL);
   ssccRenderer.append(sb, "- Stash: '" + matchedEmail("${" + SSCCRenderer.SSCCVariable.STASH_EMAIL + "}")
     + "' != Commit: '" + ssccChangeSet.getAuthor().getEmailAddress() + "'" + NL);
   if (settings.getRequireMatchingAuthorEmailMessage().isPresent()) {
    ssccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorEmailMessage().get() + NL);
   }
  }
  if (!refChangeVerificationResult.getSsccChangeSets().get(ssccChangeSet).getEmailCommitterResult()) {
   ssccRenderer.append(sb, NL);
   ssccRenderer.append(sb, "- Stash: '" + matchedEmail("${" + SSCCRenderer.SSCCVariable.STASH_EMAIL + "}")
     + "' != Commit: '" + ssccChangeSet.getCommitter().getEmailAddress() + "'" + NL);
   if (settings.getRequireMatchingAuthorEmailMessage().isPresent()) {
    ssccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorEmailMessage().get() + NL);
   }
  }
  if (!refChangeVerificationResult.getSsccChangeSets().get(ssccChangeSet).isValidateChangeSetForAuthorEmailInStash()) {
   ssccRenderer.append(sb, NL);
   ssccRenderer.append(sb, "- Commit: '" + ssccChangeSet.getAuthor().getEmailAddress() + "'" + NL);
   if (settings.getRequireMatchingAuthorEmailMessage().isPresent()) {
    ssccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorEmailMessage().get() + NL);
   }
  }
 }

 private String matchedEmail(String emailAddress) {
  if (settings.getRequireMatchingAuthorEmailRegexp().isPresent()) {
   return ssccRenderer.render(settings.getRequireMatchingAuthorEmailRegexp().get());
  }
  return ssccRenderer.render(emailAddress);
 }

 private void printNameVerification(final SSCCRefChangeVerificationResult refChangeVerificationResult,
   final SSCCChangeSet ssccChangeSet, StringBuilder sb) {
  if (!refChangeVerificationResult.getSsccChangeSets().get(ssccChangeSet).getNameAuthorResult()) {
   ssccRenderer.append(sb, NL);
   ssccRenderer.append(sb, "- Stash: '${" + SSCCRenderer.SSCCVariable.STASH_NAME + "}' != Commit: '"
     + ssccChangeSet.getAuthor().getName() + "'" + NL);
   if (settings.getRequireMatchingAuthorNameMessage().isPresent()) {
    ssccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorNameMessage().get() + NL);
   }
  }
  if (!refChangeVerificationResult.getSsccChangeSets().get(ssccChangeSet).getNameCommitterResult()) {
   ssccRenderer.append(sb, NL);
   ssccRenderer.append(sb, "- Stash: '${" + SSCCRenderer.SSCCVariable.STASH_NAME + "}' != Commit: '"
     + ssccChangeSet.getCommitter().getName() + "'" + NL);
   if (settings.getRequireMatchingAuthorNameMessage().isPresent()) {
    ssccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorNameMessage().get() + NL);
   }
  }
  if (!refChangeVerificationResult.getSsccChangeSets().get(ssccChangeSet).isValidateChangeSetForAuthorNameInStash()) {
   ssccRenderer.append(sb, NL);
   ssccRenderer.append(sb, "- Commit: '" + ssccChangeSet.getAuthor().getName() + "'" + NL);
   if (settings.getRequireMatchingAuthorNameMessage().isPresent()) {
    ssccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorNameMessage().get() + NL);
   }
  }
 }

 private void printBranchNameVerification(SSCCRefChangeVerificationResult refChangeVerificationResult, StringBuilder sb) {
  if (!refChangeVerificationResult.isBranchNameValid()) {
   ssccRenderer.append(sb, NL);
   ssccRenderer.append(sb, "- Branch: " + refChangeVerificationResult.getRefId() + ", "
     + settings.getBranchRejectionRegexp().get() + NL);
   if (settings.getBranchRejectionRegexpMessage().isPresent()) {
    ssccRenderer.append(sb, "  " + settings.getBranchRejectionRegexpMessage().get() + NL);
   }
  }
 }

 private void printRejectedContent(Optional<String> rejectedContent, StringBuilder sb) {
  if (rejectedContent.isPresent()) {
   ssccRenderer.append(sb, NL);
   ssccRenderer.append(sb, "- " + settings.getCommitDiffRegexp().get() + ":" + NL);
   ssccRenderer.append(sb, rejectedContent.get().replaceAll("$", "\n  ") + NL);
   if (settings.getCommitDiffRegexpMessage().isPresent()) {
    ssccRenderer.append(sb, "  " + settings.getCommitDiffRegexpMessage().get() + NL);
   }
  }
 }

 private void printMaximumSizeExceeded(Map<String, Long> map, StringBuilder sb) {
  for (String file : map.keySet()) {
   Long sizeKb = map.get(file);
   ssccRenderer.append(sb, NL);
   ssccRenderer.append(sb, "- " + file + " " + sizeKb + "kb > " + settings.getCommitSizeKb() + "kb" + NL);
   if (settings.getCommitSizeMessage().isPresent()) {
    ssccRenderer.append(sb, "  " + settings.getCommitSizeMessage().get() + NL);
   }
  }
 }

 private void printRefChange(final SSCCRefChangeVerificationResult refChangeVerificationResult, StringBuilder sb) {
  ssccRenderer.append(sb, refChangeVerificationResult.getRefId() + " "
    + refChangeVerificationResult.getFromHash().substring(0, 10) + " -> "
    + refChangeVerificationResult.getToHash().substring(0, 10) + NL);
 }

 private void printRuleMessage(final SSCCGroup ssccVerificationResult, StringBuilder sb) {
  if (ssccVerificationResult.getAccept().equals(SHOW_MESSAGE)) {
   if (ssccVerificationResult.getMessage().isPresent()) {
    ssccRenderer.append(sb, NL);
    ssccRenderer.append(sb, "- " + ssccVerificationResult.getMessage().get() + NL);
   }
  }
 }

 private void printJqlVerification(SSCCRefChangeVerificationResult refChangeVerificationResult,
   SSCCChangeSet ssccChangeSet, StringBuilder sb) {
  for (String query : refChangeVerificationResult.getSsccChangeSets().get(ssccChangeSet).getFailingJqls()) {
   ssccRenderer.append(sb, NL);
   ssccRenderer.append(sb, "- JQL: " + query + NL);
   if (settings.getJqlCheckMessage().isPresent()) {
    ssccRenderer.append(sb, "  " + settings.getJqlCheckMessage().get() + NL);
   }
  }
 }

 public String printVerificationResults(SSCCVerificationResult verificationResult) {
  StringBuilder sb = new StringBuilder();
  if (verificationResult.isAccepted()) {
   if (settings.getAcceptMessage().isPresent()) {
    ssccRenderer.append(sb, ssccRenderer.render(settings.getAcceptMessage().get()) + NL);
   }
  } else {
   if (settings.getRejectMessage().isPresent()) {
    ssccRenderer.append(sb, settings.getRejectMessage().get() + NL);
   }
  }

  for (final SSCCRefChangeVerificationResult refChangeVerificationResult : verificationResult.getRefChanges()) {
   if (!refChangeVerificationResult.hasReportables()) {
    continue;
   }
   printRefChange(refChangeVerificationResult, sb);
   printBranchNameVerification(refChangeVerificationResult, sb);
   for (final SSCCChangeSet ssccChangeSet : refChangeVerificationResult.getSsccChangeSets().keySet()) {
    SSCCChangeSetVerificationResult changeSetVerificationResult = refChangeVerificationResult.getSsccChangeSets().get(
      ssccChangeSet);
    if (!changeSetVerificationResult.hasReportables()) {
     continue;
    }
    printCommit(ssccChangeSet, sb);
    printEmailVerification(refChangeVerificationResult, ssccChangeSet, sb);
    printNameVerification(refChangeVerificationResult, ssccChangeSet, sb);
    printJqlVerification(refChangeVerificationResult, ssccChangeSet, sb);
    printMaximumSizeExceeded(changeSetVerificationResult.getExceeding(), sb);
    printRejectedContent(changeSetVerificationResult.getRejectedContent(), sb);
    for (final SSCCGroup ssccVerificationResult : changeSetVerificationResult.getGroupsResult().keySet()) {
     printAcceptMessages(ssccVerificationResult, sb);
     printRuleMessage(ssccVerificationResult, sb);
    }
   }
  }

  if (settings.getAcceptMessage().isPresent() || !verificationResult.isAccepted()) {
   ssccRenderer.append(sb, NL);
  }
  return sb.toString();
 }
}
