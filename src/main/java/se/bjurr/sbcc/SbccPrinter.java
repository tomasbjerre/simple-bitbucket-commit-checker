package se.bjurr.sbcc;

import static se.bjurr.sbcc.settings.SbccGroup.Accept.ACCEPT;
import static se.bjurr.sbcc.settings.SbccGroup.Accept.SHOW_MESSAGE;

import java.util.Map;

import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.data.SbccChangeSetVerificationResult;
import se.bjurr.sbcc.data.SbccRefChangeVerificationResult;
import se.bjurr.sbcc.data.SbccVerificationResult;
import se.bjurr.sbcc.settings.SbccGroup;
import se.bjurr.sbcc.settings.SbccRule;
import se.bjurr.sbcc.settings.SbccSettings;

import com.google.common.base.Optional;

public class SbccPrinter {

  private final SbccSettings settings;
  private final SbccRenderer sbccRenderer;
  public static String NL = System.getProperty("line.separator");

  public SbccPrinter(SbccSettings settings, SbccRenderer sbccRenderer) {
    this.settings = settings;
    this.sbccRenderer = sbccRenderer;
  }

  private void printAcceptMessages(final SbccGroup sbccVerificationResult, StringBuilder sb) {
    if (sbccVerificationResult.getAccept().equals(ACCEPT)) {
      sbccRenderer.append(sb, NL);
      if (sbccVerificationResult.getMessage().isPresent()) {
        sbccRenderer.append(sb, "- " + sbccVerificationResult.getMessage().get() + NL);
      }
      for (final SbccRule sbccRule : sbccVerificationResult.getRules()) {
        if (sbccRule.getMessage().isPresent()) {
          sbccRenderer.append(
              sb, "  " + sbccRule.getMessage().get() + ": " + sbccRule.getRegexp() + NL);
        }
      }
    }
  }

  private void printCommit(final SbccChangeSet sbccChangeSet, StringBuilder sb) {
    sbccRenderer.append(sb, NL);
    sbccRenderer.append(sb, NL);
    sbccRenderer.append(
        sb,
        sbccChangeSet.getId()
            + " "
            + sbccChangeSet.getAuthor().getName()
            + " <"
            + sbccChangeSet.getAuthor().getEmailAddress()
            + ">"
            + NL);
    sbccRenderer.append(sb, ">>> " + sbccChangeSet.getMessage() + NL);
  }

  private void printEmailVerification(
      final SbccRefChangeVerificationResult refChangeVerificationResult,
      final SbccChangeSet sbccChangeSet,
      StringBuilder sb) {
    if (!refChangeVerificationResult
        .getSbccChangeSets()
        .get(sbccChangeSet)
        .getEmailAuthorResult()) {
      sbccRenderer.append(sb, NL);
      sbccRenderer.append(
          sb,
          "- Bitbucket: '"
              + matchedEmail("${" + SbccRenderer.SBCCVariable.BITBUCKET_EMAIL + "}")
              + "' != Commit: '"
              + sbccChangeSet.getAuthor().getEmailAddress()
              + "'"
              + NL);
      if (settings.getRequireMatchingAuthorEmailMessage().isPresent()) {
        sbccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorEmailMessage().get() + NL);
      }
    }
    if (!refChangeVerificationResult
        .getSbccChangeSets()
        .get(sbccChangeSet)
        .getEmailCommitterResult()) {
      sbccRenderer.append(sb, NL);
      sbccRenderer.append(
          sb,
          "- Bitbucket: '"
              + matchedEmail("${" + SbccRenderer.SBCCVariable.BITBUCKET_EMAIL + "}")
              + "' != Commit: '"
              + sbccChangeSet.getCommitter().getEmailAddress()
              + "'"
              + NL);
      if (settings.getRequireMatchingAuthorEmailMessage().isPresent()) {
        sbccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorEmailMessage().get() + NL);
      }
    }
    if (!refChangeVerificationResult
        .getSbccChangeSets()
        .get(sbccChangeSet)
        .isValidateChangeSetForAuthorEmailInBitbucket()) {
      sbccRenderer.append(sb, NL);
      sbccRenderer.append(
          sb, "- Commit: '" + sbccChangeSet.getAuthor().getEmailAddress() + "'" + NL);
      if (settings.getRequireMatchingAuthorEmailMessage().isPresent()) {
        sbccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorEmailMessage().get() + NL);
      }
    }
  }

  private String matchedEmail(String emailAddress) {
    if (settings.getRequireMatchingAuthorEmailRegexp().isPresent()) {
      return sbccRenderer.render(settings.getRequireMatchingAuthorEmailRegexp().get());
    }
    return sbccRenderer.render(emailAddress);
  }

  private void printNameVerification(
      final SbccRefChangeVerificationResult refChangeVerificationResult,
      final SbccChangeSet sbccChangeSet,
      StringBuilder sb) {
    if (!refChangeVerificationResult.getSbccChangeSets().get(sbccChangeSet).getNameAuthorResult()) {
      sbccRenderer.append(sb, NL);
      sbccRenderer.append(
          sb,
          "- Bitbucket: '${"
              + SbccRenderer.SBCCVariable.BITBUCKET_NAME
              + "}' != Commit: '"
              + sbccChangeSet.getAuthor().getName()
              + "'"
              + NL);
      if (settings.getRequireMatchingAuthorNameMessage().isPresent()) {
        sbccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorNameMessage().get() + NL);
      }
    }
    if (!refChangeVerificationResult
        .getSbccChangeSets()
        .get(sbccChangeSet)
        .getNameCommitterResult()) {
      sbccRenderer.append(sb, NL);
      sbccRenderer.append(
          sb,
          "- Bitbucket: '${"
              + SbccRenderer.SBCCVariable.BITBUCKET_NAME
              + "}' != Commit: '"
              + sbccChangeSet.getCommitter().getName()
              + "'"
              + NL);
      if (settings.getRequireMatchingAuthorNameMessage().isPresent()) {
        sbccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorNameMessage().get() + NL);
      }
    }
    if (!refChangeVerificationResult
        .getSbccChangeSets()
        .get(sbccChangeSet)
        .isValidateChangeSetForAuthorNameInBitbucket()) {
      sbccRenderer.append(sb, NL);
      sbccRenderer.append(sb, "- Commit: '" + sbccChangeSet.getAuthor().getName() + "'" + NL);
      if (settings.getRequireMatchingAuthorNameMessage().isPresent()) {
        sbccRenderer.append(sb, "  " + settings.getRequireMatchingAuthorNameMessage().get() + NL);
      }
    }
  }

  private void printBranchNameVerification(
      SbccRefChangeVerificationResult refChangeVerificationResult, StringBuilder sb) {
    if (!refChangeVerificationResult.isBranchNameValid()) {
      sbccRenderer.append(sb, NL);
      sbccRenderer.append(
          sb,
          "- Branch: "
              + refChangeVerificationResult.getRefId()
              + ", "
              + settings.getBranchRejectionRegexp().get()
              + NL);
      if (settings.getBranchRejectionRegexpMessage().isPresent()) {
        sbccRenderer.append(sb, "  " + settings.getBranchRejectionRegexpMessage().get() + NL);
      }
    }
  }

  private void printRejectedContent(Optional<String> rejectedContent, StringBuilder sb) {
    if (rejectedContent.isPresent()) {
      sbccRenderer.append(sb, NL);
      sbccRenderer.append(sb, "- " + settings.getCommitDiffRegexp().get() + ":" + NL);
      sbccRenderer.append(sb, rejectedContent.get().replaceAll("$", "\n  ") + NL);
      if (settings.getCommitDiffRegexpMessage().isPresent()) {
        sbccRenderer.append(sb, "  " + settings.getCommitDiffRegexpMessage().get() + NL);
      }
    }
  }

  private void printMaximumSizeExceeded(Map<String, Long> map, StringBuilder sb) {
    for (String file : map.keySet()) {
      Long sizeKb = map.get(file);
      sbccRenderer.append(sb, NL);
      sbccRenderer.append(
          sb, "- " + file + " " + sizeKb + "kb > " + settings.getCommitSizeKb() + "kb" + NL);
      if (settings.getCommitSizeMessage().isPresent()) {
        sbccRenderer.append(sb, "  " + settings.getCommitSizeMessage().get() + NL);
      }
    }
  }

  private void printRefChange(
      final SbccRefChangeVerificationResult refChangeVerificationResult, StringBuilder sb) {
    sbccRenderer.append(
        sb,
        refChangeVerificationResult.getRefId()
            + " "
            + refChangeVerificationResult.getFromHash().substring(0, 10)
            + " -> "
            + refChangeVerificationResult.getToHash().substring(0, 10)
            + NL);
  }

  private void printRuleMessage(final SbccGroup sbccVerificationResult, StringBuilder sb) {
    if (sbccVerificationResult.getAccept().equals(SHOW_MESSAGE)) {
      if (sbccVerificationResult.getMessage().isPresent()) {
        sbccRenderer.append(sb, NL);
        sbccRenderer.append(sb, "- " + sbccVerificationResult.getMessage().get() + NL);
      }
    }
  }

  private void printJqlVerification(
      SbccRefChangeVerificationResult refChangeVerificationResult,
      SbccChangeSet sbccChangeSet,
      StringBuilder sb) {
    for (String query :
        refChangeVerificationResult.getSbccChangeSets().get(sbccChangeSet).getFailingJqls()) {
      sbccRenderer.append(sb, NL);
      sbccRenderer.append(sb, "- JQL: " + query + NL);
      if (settings.getJqlCheckMessage().isPresent()) {
        sbccRenderer.append(sb, "  " + settings.getJqlCheckMessage().get() + NL);
      }
    }
  }

  public String printVerificationResults(SbccVerificationResult verificationResult) {
    StringBuilder sb = new StringBuilder();
    if (verificationResult.isAccepted()) {
      if (settings.getAcceptMessage().isPresent()) {
        sbccRenderer.append(sb, sbccRenderer.render(settings.getAcceptMessage().get()) + NL);
      }
    } else {
      if (settings.getRejectMessage().isPresent()) {
        sbccRenderer.append(sb, settings.getRejectMessage().get() + NL);
      }
    }

    for (final SbccRefChangeVerificationResult refChangeVerificationResult :
        verificationResult.getRefChanges()) {
      if (!refChangeVerificationResult.hasReportables()) {
        continue;
      }
      printRefChange(refChangeVerificationResult, sb);
      printBranchNameVerification(refChangeVerificationResult, sb);
      for (final SbccChangeSet sbccChangeSet :
          refChangeVerificationResult.getSbccChangeSets().keySet()) {
        sbccRenderer.setSbccChangeSet(sbccChangeSet);
        SbccChangeSetVerificationResult changeSetVerificationResult =
            refChangeVerificationResult.getSbccChangeSets().get(sbccChangeSet);
        if (!changeSetVerificationResult.hasReportables()) {
          continue;
        }
        printCommit(sbccChangeSet, sb);
        printEmailVerification(refChangeVerificationResult, sbccChangeSet, sb);
        printNameVerification(refChangeVerificationResult, sbccChangeSet, sb);
        printJqlVerification(refChangeVerificationResult, sbccChangeSet, sb);
        printMaximumSizeExceeded(changeSetVerificationResult.getExceeding(), sb);
        printRejectedContent(changeSetVerificationResult.getRejectedContent(), sb);
        for (final SbccGroup sbccVerificationResult :
            changeSetVerificationResult.getGroupsResult().keySet()) {
          printAcceptMessages(sbccVerificationResult, sb);
          printRuleMessage(sbccVerificationResult, sb);
        }
        sbccRenderer.setSbccChangeSet(null);
      }
    }

    if (settings.getAcceptMessage().isPresent() || !verificationResult.isAccepted()) {
      sbccRenderer.append(sb, NL);
    }
    return sb.toString();
  }
}
