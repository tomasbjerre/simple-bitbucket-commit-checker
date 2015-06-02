package se.bjurr.sscc.settings;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;
import static se.bjurr.sscc.settings.SSCCGroup.ssccGroup;
import static se.bjurr.sscc.settings.SSCCRule.ssccRule;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import com.atlassian.stash.setting.Settings;
import com.google.common.base.Optional;
import com.google.gson.GsonBuilder;

public class SSCCSettings {
 public static final String SETTING_ACCEPT_MESSAGE = "acceptMessage";
 public static final String SETTING_BRANCHES = "branches";
 public static final String SETTING_DRY_RUN = "dryRun";
 public static final String SETTING_DRY_RUN_MESSAGE = "dryRunMessage";
 public static final String SETTING_EXCLUDE_MERGE_COMMITS = "excludeMergeCommits";
 public static final String SETTING_EXCLUDE_TAG_COMMITS = "excludeTagCommits";
 public static final String SETTING_GROUP_ACCEPT = "groupAccept";
 public static final String SETTING_GROUP_MATCH = "groupMatch";
 public static final String SETTING_GROUP_MESSAGE = "groupMessage";
 public static final String SETTING_REJECT_MESSAGE = "rejectMessage";
 public static final String SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL = "requireMatchingCommitterEmail";
 public static final String SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL = "requireMatchingAuthorEmail";
 public static final String SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE = "requireMatchingAuthorEmailMessage";
 public static final String SETTING_REQUIRE_MATCHING_AUTHOR_NAME = "requireMatchingAuthorName";
 public static final String SETTING_REQUIRE_MATCHING_COMMITTER_NAME = "requireMatchingCommitterName";
 public static final String SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE = "requireMatchingAuthorNameMessage";
 public static final String SETTING_RULE_MESSAGE = "ruleMessage";
 public static final String SETTING_RULE_REGEXP = "ruleRegexp";
 public static final String SETTING_DIFF_REGEXP = "checkCommitDiffRegexp";
 public static final String SETTING_DIFF_REGEXP_MESSAGE = "checkCommitDiffRegexpMessage";
 public static final String SETTING_SIZE = "checkCommitSize";
 public static final String SETTING_SIZE_MESSAGE = "checkCommitSizeMessage";
 public static final String SETTING_BRANCH_REJECTION_REGEXP = "branchRejectionRegexp";
 public static final String SETTING_BRANCH_REJECTION_REGEXP_MESSAGE = "branchRejectionRegexpMessage";
 public static final String SETTING_ALLOW_SERVICE_USERS = "allowServiceUsers";

 private String commitDiffRegexp;
 private String commitDiffRegexpMessage;
 private String commitSizeMessage;
 private int commitSize;
 private String acceptMessage;
 private String branches;
 private boolean dryRun;
 private String dryRunMessage;
 private boolean excludeMergeCommits;
 private Boolean excludeTagCommits;
 private final List<SSCCGroup> groups = newArrayList();
 private String rejectMessage;
 private boolean requireMatchingAuthorEmail;
 private boolean requireMatchingCommitterEmail;
 private String requireMatchingAuthorEmailMessage;
 private boolean requireMatchingAuthorName;
 private boolean requireMatchingCommitterName;
 private String requireMatchingAuthorNameMessage;
 private boolean requireOnlyOneIssue;
 private String requireOnlyOneIssueMessage;
 private String branchRejectionRegexp;
 private String branchRejectionRegexpMessage;
 private boolean allowServiceUsers;

 public static SSCCSettings sscSettings(Settings settings) throws ValidationException {
  final SSCCSettings ssccSettings = new SSCCSettings();
  ssccSettings
    .withAcceptMessage( //
      settings.getString(SETTING_ACCEPT_MESSAGE))
    .withBranches(validateRegExp(SETTING_BRANCHES, settings.getString(SETTING_BRANCHES)))
    .withDryRun(settings.getBoolean(SETTING_DRY_RUN))
    .withDryRunMessage(settings.getString(SETTING_DRY_RUN_MESSAGE))
    .withExcludeMergeCommits(settings.getBoolean(SETTING_EXCLUDE_MERGE_COMMITS))
    .withExcludeTagCommits(settings.getBoolean(SETTING_EXCLUDE_TAG_COMMITS))
    .withRejectMessage(settings.getString(SETTING_REJECT_MESSAGE))
    .withRequireMatchingAuthorEmail(settings.getBoolean(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL))
    .withRequireMatchingCommitterEmail(settings.getBoolean(SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL))
    .withRequireMatchingAuthorEmailMessage(settings.getString(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE))
    .withRequireMatchingAuthorName(settings.getBoolean(SETTING_REQUIRE_MATCHING_AUTHOR_NAME))
    .withRequireMatchingCommitterName(settings.getBoolean(SETTING_REQUIRE_MATCHING_COMMITTER_NAME))
    .withRequireMatchingAuthorNameMessage(settings.getString(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE))
    .withCheckCommitDiffRegexp(validateRegExp(SETTING_DIFF_REGEXP, settings.getString(SETTING_DIFF_REGEXP)))
    .withCheckCommitDiffRegexpMessage(settings.getString(SETTING_DIFF_REGEXP_MESSAGE))
    .withCheckCommitSizeMessage(settings.getString(SETTING_SIZE_MESSAGE))
    .withBranchRejectionRegexp(
      validateRegExp(SETTING_BRANCH_REJECTION_REGEXP, settings.getString(SETTING_BRANCH_REJECTION_REGEXP)))
    .withBranchRejectionRegexpMessage(settings.getString(SETTING_BRANCH_REJECTION_REGEXP_MESSAGE))
    .withAllowServiceUsers(settings.getBoolean(SETTING_ALLOW_SERVICE_USERS));
  try {
   if (!isNullOrEmpty(settings.getString(SETTING_SIZE))) {
    ssccSettings.withCheckCommitSize(parseInt(settings.getString(SETTING_SIZE)));
   }
  } catch (Exception e) {
   throw new ValidationException(SETTING_SIZE, "Not an integer!");
  }
  for (int g = 0; g < 1000; g++) {
   final Optional<String> accept = fromNullable(settings.getString(SETTING_GROUP_ACCEPT + "[" + g + "]"));
   final Optional<String> match = fromNullable(settings.getString(SETTING_GROUP_MATCH + "[" + g + "]"));
   final String message = settings.getString(SETTING_GROUP_MESSAGE + "[" + g + "]");
   if (accept.isPresent() || match.isPresent()) {
    if (accept.isPresent() && !match.isPresent()) {
     throw new ValidationException(SETTING_GROUP_MATCH + "[" + g + "]",
       "Cannot add a rule group without matching criteria!");
    } else if (!accept.isPresent() && match.isPresent()) {
     throw new ValidationException(SETTING_GROUP_ACCEPT + "[" + g + "]",
       "Cannot add a rule group without acceptance criteria!");
    }
    final List<SSCCRule> rules = newArrayList();
    for (int r = 0; r < 1000; r++) {
     final String ruleRegexpField = SETTING_RULE_REGEXP + "[" + g + "][" + r + "]";
     final Optional<String> ruleRegexp = fromNullable(settings.getString(ruleRegexpField));
     final Optional<String> ruleMessage = fromNullable(settings.getString(SETTING_RULE_MESSAGE + "[" + g + "][" + r
       + "]"));
     if (ruleRegexp.isPresent()) {
      rules.add(ssccRule()//
        .withRegexp(validateRegExp(ruleRegexpField, ruleRegexp.get())) //
        .withMessage(ruleMessage.orNull()));
     } else if (ruleMessage.isPresent()) {
      throw new ValidationException(ruleRegexpField, "Cannot add a rule without regexp!");
     }
    }

    ssccSettings.withGroup( //
      ssccGroup() //
        .withAccept(accept.get()) //
        .withMatch(match.get()) //
        .withMessage(message) //
        .withRules(rules));
   }
  }
  return ssccSettings;
 }

 private void withAllowServiceUsers(Boolean allowServiceUsers) {
  this.allowServiceUsers = firstNonNull(allowServiceUsers, FALSE);
 }

 private SSCCSettings withCheckCommitDiffRegexp(String string) {
  this.commitDiffRegexp = emptyToNull(nullToEmpty(string).trim());
  return this;
 }

 private SSCCSettings withCheckCommitDiffRegexpMessage(String string) {
  this.commitDiffRegexpMessage = emptyToNull(nullToEmpty(string).trim());
  return this;
 }

 private SSCCSettings withBranchRejectionRegexp(String string) {
  this.branchRejectionRegexp = emptyToNull(nullToEmpty(string).trim());
  return this;
 }

 private SSCCSettings withBranchRejectionRegexpMessage(String string) {
  this.branchRejectionRegexpMessage = emptyToNull(nullToEmpty(string).trim());
  return this;
 }

 private SSCCSettings withCheckCommitSize(int commitSize) {
  this.commitSize = commitSize;
  return this;
 }

 private SSCCSettings withCheckCommitSizeMessage(String string) {
  this.commitSizeMessage = emptyToNull(nullToEmpty(string).trim());
  return this;
 }

 private static String validateRegExp(String field, String regexp) throws ValidationException {
  if (regexp == null) {
   return null;
  }
  try {
   compile(regexp);
  } catch (final PatternSyntaxException ex) {
   throw new ValidationException(field, "Invalid Regexp: " + ex.getMessage().replaceAll("\n", " "));
  }
  return regexp;
 }

 private SSCCSettings() {
 }

 public Optional<String> getAcceptMessage() {
  return fromNullable(acceptMessage);
 }

 public Optional<String> getBranches() {
  return fromNullable(branches);
 }

 public Optional<String> getDryRunMessage() {
  return fromNullable(dryRunMessage);
 }

 public List<SSCCGroup> getGroups() {
  return groups;
 }

 public Optional<String> getRejectMessage() {
  return fromNullable(rejectMessage);
 }

 public Optional<String> getRequireMatchingAuthorEmailMessage() {
  return fromNullable(requireMatchingAuthorEmailMessage);
 }

 public Optional<String> getRequireMatchingAuthorNameMessage() {
  return fromNullable(requireMatchingAuthorNameMessage);
 }

 public boolean getRequireOnlyOneIssue() {
  return requireOnlyOneIssue;
 }

 public Optional<String> getRequireOnlyOneIssueMessage() {
  return fromNullable(requireOnlyOneIssueMessage);
 }

 public boolean isDryRun() {
  return dryRun;
 }

 public boolean shouldExcludeMergeCommits() {
  return excludeMergeCommits;
 }

 public Boolean shouldExcludeTagCommits() {
  return excludeTagCommits;
 }

 public boolean shouldRequireMatchingAuthorEmail() {
  return requireMatchingAuthorEmail;
 }

 public boolean shouldRequireMatchingAuthorName() {
  return requireMatchingAuthorName;
 }

 public boolean shouldRequireMatchingCommitterEmail() {
  return requireMatchingCommitterEmail;
 }

 public boolean shouldRequireMatchingCommitterName() {
  return requireMatchingCommitterName;
 }

 public Optional<String> getCommitDiffRegexp() {
  return fromNullable(commitDiffRegexp);
 }

 public Optional<String> getCommitDiffRegexpMessage() {
  return fromNullable(commitDiffRegexpMessage);
 }

 public int getCommitSizeKb() {
  if (commitSize == 0) {
   return MAX_VALUE;
  } else {
   return commitSize;
  }
 }

 public Optional<String> getCommitSizeMessage() {
  return fromNullable(commitSizeMessage);
 }

 @Override
 public String toString() {
  return new GsonBuilder().setPrettyPrinting().create().toJson(this);
 }

 private SSCCSettings withAcceptMessage(String acceptMessage) {
  this.acceptMessage = emptyToNull(acceptMessage);
  return this;
 }

 private SSCCSettings withBranches(String branches) {
  this.branches = emptyToNull(branches);
  return this;
 }

 private SSCCSettings withDryRun(Boolean dryRun) {
  this.dryRun = firstNonNull(dryRun, FALSE);
  return this;
 }

 private SSCCSettings withDryRunMessage(String dryRunMessage) {
  this.dryRunMessage = emptyToNull(dryRunMessage);
  return this;
 }

 private SSCCSettings withExcludeMergeCommits(Boolean excludeMergeCommits) {
  this.excludeMergeCommits = firstNonNull(excludeMergeCommits, TRUE);
  return this;
 }

 private SSCCSettings withExcludeTagCommits(Boolean excludeTagCommits) {
  this.excludeTagCommits = firstNonNull(excludeTagCommits, TRUE);
  return this;
 }

 private SSCCSettings withGroup(SSCCGroup ssccGroup) {
  this.groups.add(ssccGroup);
  return this;
 }

 private SSCCSettings withRejectMessage(String rejectMessage) {
  this.rejectMessage = emptyToNull(rejectMessage);
  return this;
 }

 private SSCCSettings withRequireMatchingAuthorEmail(Boolean requireMatchingAuthorEmail) {
  this.requireMatchingAuthorEmail = firstNonNull(requireMatchingAuthorEmail, FALSE);
  return this;
 }

 private SSCCSettings withRequireMatchingCommitterEmail(Boolean requireMatchingCommitterEmail) {
  this.requireMatchingCommitterEmail = firstNonNull(requireMatchingCommitterEmail, FALSE);
  return this;
 }

 private SSCCSettings withRequireMatchingAuthorEmailMessage(String requireMatchingAuthorEmailMessage) {
  this.requireMatchingAuthorEmailMessage = emptyToNull(requireMatchingAuthorEmailMessage);
  return this;
 }

 private SSCCSettings withRequireMatchingAuthorName(Boolean requireMatchingAuthorName) {
  this.requireMatchingAuthorName = firstNonNull(requireMatchingAuthorName, FALSE);
  return this;
 }

 private SSCCSettings withRequireMatchingCommitterName(Boolean requireMatchingCommitterName) {
  this.requireMatchingCommitterName = firstNonNull(requireMatchingCommitterName, FALSE);
  return this;
 }

 private SSCCSettings withRequireMatchingAuthorNameMessage(String requireMatchingAuthorNameMessage) {
  this.requireMatchingAuthorNameMessage = emptyToNull(requireMatchingAuthorNameMessage);
  return this;
 }

 public Optional<String> getBranchRejectionRegexp() {
  return fromNullable(branchRejectionRegexp);
 }

 public Optional<String> getBranchRejectionRegexpMessage() {
  return fromNullable(branchRejectionRegexpMessage);
 }

 public boolean allowServiceUsers() {
  return allowServiceUsers;
 }
}
