package se.bjurr.sbcc.settings;

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
import static se.bjurr.sbcc.settings.SbccGroup.sbccGroup;
import static se.bjurr.sbcc.settings.SbccRule.sbccRule;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import com.atlassian.bitbucket.setting.Settings;
import com.google.common.base.Optional;

public class SbccSettings {
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
  public static final String SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL =
      "requireMatchingCommitterEmail";
  public static final String SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL = "requireMatchingAuthorEmail";
  public static final String SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_BITBUCKET =
      "requireMatchingAuthorEmailInBitbucket";
  public static final String SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_REGEXP =
      "requireMatchingAuthorEmailRegexp";
  public static final String SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE =
      "requireMatchingAuthorEmailMessage";
  public static final String SETTING_REQUIRE_MATCHING_AUTHOR_NAME = "requireMatchingAuthorName";
  public static final String SETTING_REQUIRE_MATCHING_AUTHOR_NAME_SLUG =
      "requireMatchingAuthorNameSlug";
  public static final String SETTING_REQUIRE_MATCHING_AUTHOR_NAME_BITBUCKET =
      "requireMatchingAuthorNameInBitbucket";
  public static final String SETTING_REQUIRE_MATCHING_AUTHOR_NAME_BITBUCKET_SLUG =
      "requireMatchingAuthorNameInBitbucketSlug";
  public static final String SETTING_REQUIRE_MATCHING_COMMITTER_NAME =
      "requireMatchingCommitterName";
  public static final String SETTING_REQUIRE_MATCHING_COMMITTER_NAME_SLUG =
      "requireMatchingCommitterNameSlug";
  public static final String SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE =
      "requireMatchingAuthorNameMessage";
  public static final String SETTING_RULE_MESSAGE = "ruleMessage";
  public static final String SETTING_RULE_REGEXP = "ruleRegexp";
  public static final String SETTING_DIFF_REGEXP = "checkCommitDiffRegexp";
  public static final String SETTING_DIFF_REGEXP_MESSAGE = "checkCommitDiffRegexpMessage";
  public static final String SETTING_SIZE = "checkCommitSize";
  public static final String SETTING_SIZE_MESSAGE = "checkCommitSizeMessage";
  public static final String SETTING_BRANCH_REJECTION_REGEXP = "branchRejectionRegexp";
  public static final String SETTING_BRANCH_REJECTION_REGEXP_MESSAGE =
      "branchRejectionRegexpMessage";
  public static final String SETTING_ALLOW_SERVICE_USERS = "allowServiceUsers";
  public static final String SETTING_JQL_CHECK = "jqlCheck";
  public static final String SETTING_JQL_CHECK_MESSAGE = "jqlCheckMessage";
  public static final String SETTING_COMMIT_REGEXP = "commitRegexp";
  public static final String SETTING_JQL_CHECK_QUERY = "jqlCheckQuery";
  public static final String SETTING_CHECK_PULLREQUESTS = "shouldCheckPullrequests";
  public static final String SETTING_CHECK_PULLREQUESTS_MESSAGE = "shouldCheckPullrequestsMessage";
  public static final String SETTING_IGNORE_USERS_PATTERN = "ignoreUsersPattern";

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
  private final List<SbccGroup> groups = newArrayList();
  private String rejectMessage;
  private boolean requireMatchingAuthorEmail;
  private boolean requireMatchingCommitterEmail;
  private String requireMatchingAuthorEmailMessage;
  private boolean requireMatchingAuthorName;
  private boolean requireMatchingAuthorNameSlug;
  private boolean requireMatchingCommitterName;
  private boolean requireMatchingCommitterNameSlug;
  private String requireMatchingAuthorNameMessage;
  private boolean requireOnlyOneIssue;
  private String requireOnlyOneIssueMessage;
  private String branchRejectionRegexp;
  private String branchRejectionRegexpMessage;
  private boolean allowServiceUsers;
  private String requireMatchingAuthorEmailRegexp;
  private Boolean jqlCheck;
  private String jqlCheckMessage;
  private String jqlCheckQuery;
  private String commitRegexp;
  private Boolean requireMatchingAuthorEmailInBitbucket;
  private Boolean requireMatchingAuthorNameInBitbucket;
  private Boolean requireMatchingAuthorNameInBitbucketSlug;
  private boolean shouldCheckPullRequests;
  private String shouldCheckPullRequestsMessage;
  private String ignoreUsersPattern;

  public static SbccSettings sscSettings(Settings settings) throws ValidationException {
    final SbccSettings sbccSettings = new SbccSettings();
    sbccSettings
        .withAcceptMessage( //
            settings.getString(SETTING_ACCEPT_MESSAGE))
        .withBranches(validateRegExp(SETTING_BRANCHES, settings.getString(SETTING_BRANCHES)))
        .withDryRun(settings.getBoolean(SETTING_DRY_RUN))
        .withDryRunMessage(settings.getString(SETTING_DRY_RUN_MESSAGE))
        .withExcludeMergeCommits(settings.getBoolean(SETTING_EXCLUDE_MERGE_COMMITS))
        .withExcludeTagCommits(settings.getBoolean(SETTING_EXCLUDE_TAG_COMMITS))
        .withRejectMessage(settings.getString(SETTING_REJECT_MESSAGE))
        .withRequireMatchingAuthorEmail(settings.getBoolean(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL))
        .withRequireMatchingAuthorEmailInBitbucket(
            settings.getBoolean(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_BITBUCKET))
        .withRequireMatchingCommitterEmail(
            settings.getBoolean(SETTING_REQUIRE_MATCHING_COMMITTER_EMAIL))
        .withRequireMatchingAuthorEmailMessage(
            settings.getString(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_MESSAGE))
        .withRequireMatchingAuthorEmailRegexp(
            settings.getString(SETTING_REQUIRE_MATCHING_AUTHOR_EMAIL_REGEXP))
        .withRequireMatchingAuthorName(settings.getBoolean(SETTING_REQUIRE_MATCHING_AUTHOR_NAME))
        .withRequireMatchingAuthorNameInBitbucket(
            settings.getBoolean(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_BITBUCKET))
        .withRequireMatchingAuthorNameSlug(
            settings.getBoolean(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_SLUG))
        .withRequireMatchingAuthorNameInBitbucketSlug(
            settings.getBoolean(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_BITBUCKET_SLUG))
        .withRequireMatchingCommitterName(
            settings.getBoolean(SETTING_REQUIRE_MATCHING_COMMITTER_NAME))
        .withRequireMatchingCommitterNameSlug(
            settings.getBoolean(SETTING_REQUIRE_MATCHING_COMMITTER_NAME_SLUG))
        .withRequireMatchingAuthorNameMessage(
            settings.getString(SETTING_REQUIRE_MATCHING_AUTHOR_NAME_MESSAGE))
        .withCheckCommitDiffRegexp(
            validateRegExp(SETTING_DIFF_REGEXP, settings.getString(SETTING_DIFF_REGEXP)))
        .withCheckCommitDiffRegexpMessage(settings.getString(SETTING_DIFF_REGEXP_MESSAGE))
        .withCheckCommitSizeMessage(settings.getString(SETTING_SIZE_MESSAGE))
        .withBranchRejectionRegexp(
            validateRegExp(
                SETTING_BRANCH_REJECTION_REGEXP,
                settings.getString(SETTING_BRANCH_REJECTION_REGEXP)))
        .withBranchRejectionRegexpMessage(
            settings.getString(SETTING_BRANCH_REJECTION_REGEXP_MESSAGE))
        .withAllowServiceUsers(settings.getBoolean(SETTING_ALLOW_SERVICE_USERS))
        .withJqlCheck(settings.getBoolean(SETTING_JQL_CHECK))
        .withJqlCheckMessage(settings.getString(SETTING_JQL_CHECK_MESSAGE))
        .withCommitRegexp(settings.getString(SETTING_COMMIT_REGEXP))
        .withJqlCheckQuery(settings.getString(SETTING_JQL_CHECK_QUERY))
        .withShouldCheckPullRequests(settings.getBoolean(SETTING_CHECK_PULLREQUESTS))
        .withShouldCheckPullRequestsMessage(settings.getString(SETTING_CHECK_PULLREQUESTS_MESSAGE))
        .withIgnoreUsersPattern(settings.getString(SETTING_IGNORE_USERS_PATTERN));
    try {
      if (!isNullOrEmpty(settings.getString(SETTING_SIZE))) {
        sbccSettings.withCheckCommitSize(parseInt(settings.getString(SETTING_SIZE)));
      }
    } catch (Exception e) {
      throw new ValidationException(SETTING_SIZE, "Not an integer!");
    }
    for (int g = 0; g < 1000; g++) {
      final Optional<String> accept =
          fromNullable(settings.getString(SETTING_GROUP_ACCEPT + "[" + g + "]"));
      final Optional<String> match =
          fromNullable(settings.getString(SETTING_GROUP_MATCH + "[" + g + "]"));
      final String message = settings.getString(SETTING_GROUP_MESSAGE + "[" + g + "]");
      if (accept.isPresent() || match.isPresent()) {
        if (accept.isPresent() && !match.isPresent()) {
          throw new ValidationException(
              SETTING_GROUP_MATCH + "[" + g + "]",
              "Cannot add a rule group without matching criteria!");
        } else if (!accept.isPresent() && match.isPresent()) {
          throw new ValidationException(
              SETTING_GROUP_ACCEPT + "[" + g + "]",
              "Cannot add a rule group without acceptance criteria!");
        }
        final List<SbccRule> rules = newArrayList();
        for (int r = 0; r < 1000; r++) {
          final String ruleRegexpField = SETTING_RULE_REGEXP + "[" + g + "][" + r + "]";
          final Optional<String> ruleRegexp = fromNullable(settings.getString(ruleRegexpField));
          final Optional<String> ruleMessage =
              fromNullable(settings.getString(SETTING_RULE_MESSAGE + "[" + g + "][" + r + "]"));
          if (ruleRegexp.isPresent()) {
            rules.add(
                sbccRule() //
                    .withRegexp(validateRegExp(ruleRegexpField, ruleRegexp.get())) //
                    .withMessage(ruleMessage.orNull()));
          } else if (ruleMessage.isPresent()) {
            throw new ValidationException(ruleRegexpField, "Cannot add a rule without regexp!");
          }
        }

        sbccSettings.withGroup( //
            sbccGroup() //
                .withAccept(accept.get()) //
                .withMatch(match.get()) //
                .withMessage(message) //
                .withRules(rules));
      }
    }
    return sbccSettings;
  }

  private SbccSettings withRequireMatchingAuthorNameInBitbucketSlug(
      Boolean requireMatchingAuthorNameInBitbucketSlug) {
    this.requireMatchingAuthorNameInBitbucketSlug =
        firstNonNull(requireMatchingAuthorNameInBitbucketSlug, FALSE);
    return this;
  }

  private SbccSettings withRequireMatchingAuthorNameSlug(Boolean requireMatchingAuthorNameSlug) {
    this.requireMatchingAuthorNameSlug = firstNonNull(requireMatchingAuthorNameSlug, FALSE);
    return this;
  }

  private SbccSettings withRequireMatchingCommitterNameSlug(
      Boolean requireMatchingCommitterNameSlug) {
    this.requireMatchingCommitterNameSlug = firstNonNull(requireMatchingCommitterNameSlug, FALSE);
    return this;
  }

  private void withIgnoreUsersPattern(String string) {
    this.ignoreUsersPattern = emptyToNull(string);
  }

  private SbccSettings withShouldCheckPullRequestsMessage(String string) {
    this.shouldCheckPullRequestsMessage = emptyToNull(nullToEmpty(string).trim());
    return this;
  }

  public Optional<String> getShouldCheckPullRequestsMessage() {
    return fromNullable(shouldCheckPullRequestsMessage);
  }

  private SbccSettings withJqlCheck(Boolean b) {
    this.jqlCheck = firstNonNull(b, FALSE);
    return this;
  }

  private SbccSettings withJqlCheckMessage(String string) {
    this.jqlCheckMessage = emptyToNull(nullToEmpty(string).trim());
    return this;
  }

  private SbccSettings withCommitRegexp(String string) {
    this.commitRegexp = emptyToNull(nullToEmpty(string).trim());
    return this;
  }

  private SbccSettings withJqlCheckQuery(String string) {
    this.jqlCheckQuery = nullToEmpty(string).trim();
    return this;
  }

  public Boolean shouldCheckJql() {
    return jqlCheck;
  }

  public Optional<String> getJqlCheckMessage() {
    return fromNullable(jqlCheckMessage);
  }

  public Optional<String> getCommitRegexp() {
    return fromNullable(commitRegexp);
  }

  public String getJqlCheckQuery() {
    return jqlCheckQuery;
  }

  private SbccSettings withRequireMatchingAuthorEmailRegexp(String string) {
    this.requireMatchingAuthorEmailRegexp = emptyToNull(string);
    return this;
  }

  private SbccSettings withAllowServiceUsers(Boolean allowServiceUsers) {
    this.allowServiceUsers = firstNonNull(allowServiceUsers, FALSE);
    return this;
  }

  private SbccSettings withCheckCommitDiffRegexp(String string) {
    this.commitDiffRegexp = emptyToNull(nullToEmpty(string).trim());
    return this;
  }

  private SbccSettings withCheckCommitDiffRegexpMessage(String string) {
    this.commitDiffRegexpMessage = emptyToNull(nullToEmpty(string).trim());
    return this;
  }

  private SbccSettings withBranchRejectionRegexp(String string) {
    this.branchRejectionRegexp = emptyToNull(nullToEmpty(string).trim());
    return this;
  }

  private SbccSettings withBranchRejectionRegexpMessage(String string) {
    this.branchRejectionRegexpMessage = emptyToNull(nullToEmpty(string).trim());
    return this;
  }

  private SbccSettings withCheckCommitSize(int commitSize) {
    this.commitSize = commitSize;
    return this;
  }

  private SbccSettings withCheckCommitSizeMessage(String string) {
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
      throw new ValidationException(
          field, "Invalid Regexp: " + ex.getMessage().replaceAll("\n", " "));
    }
    return regexp;
  }

  private SbccSettings() {}

  public Optional<String> getAcceptMessage() {
    return fromNullable(acceptMessage);
  }

  public Optional<String> getBranches() {
    return fromNullable(branches);
  }

  public Optional<String> getDryRunMessage() {
    return fromNullable(dryRunMessage);
  }

  public List<SbccGroup> getGroups() {
    return groups;
  }

  public Optional<String> getRejectMessage() {
    return fromNullable(rejectMessage);
  }

  public Optional<String> getRequireMatchingAuthorEmailMessage() {
    return fromNullable(requireMatchingAuthorEmailMessage);
  }

  public Optional<String> getRequireMatchingAuthorEmailRegexp() {
    return fromNullable(requireMatchingAuthorEmailRegexp);
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

  private SbccSettings withAcceptMessage(String acceptMessage) {
    this.acceptMessage = emptyToNull(acceptMessage);
    return this;
  }

  private SbccSettings withBranches(String branches) {
    this.branches = emptyToNull(branches);
    return this;
  }

  private SbccSettings withDryRun(Boolean dryRun) {
    this.dryRun = firstNonNull(dryRun, FALSE);
    return this;
  }

  private SbccSettings withDryRunMessage(String dryRunMessage) {
    this.dryRunMessage = emptyToNull(dryRunMessage);
    return this;
  }

  private SbccSettings withExcludeMergeCommits(Boolean excludeMergeCommits) {
    this.excludeMergeCommits = firstNonNull(excludeMergeCommits, TRUE);
    return this;
  }

  private SbccSettings withExcludeTagCommits(Boolean excludeTagCommits) {
    this.excludeTagCommits = firstNonNull(excludeTagCommits, TRUE);
    return this;
  }

  private SbccSettings withGroup(SbccGroup sbccGroup) {
    this.groups.add(sbccGroup);
    return this;
  }

  private SbccSettings withRejectMessage(String rejectMessage) {
    this.rejectMessage = emptyToNull(rejectMessage);
    return this;
  }

  private SbccSettings withRequireMatchingAuthorEmail(Boolean requireMatchingAuthorEmail) {
    this.requireMatchingAuthorEmail = firstNonNull(requireMatchingAuthorEmail, FALSE);
    return this;
  }

  private SbccSettings withRequireMatchingAuthorEmailInBitbucket(
      Boolean requireMatchingAuthorEmailInBitbucket) {
    this.requireMatchingAuthorEmailInBitbucket =
        firstNonNull(requireMatchingAuthorEmailInBitbucket, FALSE);
    return this;
  }

  public Boolean getRequireMatchingAuthorEmailInBitbucket() {
    return requireMatchingAuthorEmailInBitbucket;
  }

  private SbccSettings withRequireMatchingCommitterEmail(Boolean requireMatchingCommitterEmail) {
    this.requireMatchingCommitterEmail = firstNonNull(requireMatchingCommitterEmail, FALSE);
    return this;
  }

  private SbccSettings withRequireMatchingAuthorEmailMessage(
      String requireMatchingAuthorEmailMessage) {
    this.requireMatchingAuthorEmailMessage = emptyToNull(requireMatchingAuthorEmailMessage);
    return this;
  }

  private SbccSettings withRequireMatchingAuthorName(Boolean requireMatchingAuthorName) {
    this.requireMatchingAuthorName = firstNonNull(requireMatchingAuthorName, FALSE);
    return this;
  }

  private SbccSettings withRequireMatchingAuthorNameInBitbucket(
      Boolean requireMatchingAuthorNameInBitbucket) {
    this.requireMatchingAuthorNameInBitbucket =
        firstNonNull(requireMatchingAuthorNameInBitbucket, FALSE);
    return this;
  }

  public Boolean getRequireMatchingAuthorNameInBitbucket() {
    return requireMatchingAuthorNameInBitbucket;
  }

  public Boolean isRequireMatchingAuthorNameInBitbucketSlug() {
    return requireMatchingAuthorNameInBitbucketSlug;
  }

  public boolean isRequireMatchingAuthorNameSlug() {
    return requireMatchingAuthorNameSlug;
  }

  public boolean isRequireMatchingCommitterNameSlug() {
    return requireMatchingCommitterNameSlug;
  }

  private SbccSettings withRequireMatchingCommitterName(Boolean requireMatchingCommitterName) {
    this.requireMatchingCommitterName = firstNonNull(requireMatchingCommitterName, FALSE);
    return this;
  }

  private SbccSettings withRequireMatchingAuthorNameMessage(
      String requireMatchingAuthorNameMessage) {
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

  private SbccSettings withShouldCheckPullRequests(Boolean shouldCheckPullRequests) {
    this.shouldCheckPullRequests = firstNonNull(shouldCheckPullRequests, FALSE);
    return this;
  }

  public boolean shouldCheckPullRequests() {
    return shouldCheckPullRequests;
  }

  public boolean shouldCheckCommitSize() {
    return commitSize != 0 && commitSize != MAX_VALUE;
  }

  public Optional<String> getIgnoreUsersPattern() {
    return fromNullable(ignoreUsersPattern);
  }

  @Override
  public String toString() {
    return "SbccSettings [commitDiffRegexp="
        + commitDiffRegexp
        + ", commitDiffRegexpMessage="
        + commitDiffRegexpMessage
        + ", commitSizeMessage="
        + commitSizeMessage
        + ", commitSize="
        + commitSize
        + ", acceptMessage="
        + acceptMessage
        + ", branches="
        + branches
        + ", dryRun="
        + dryRun
        + ", dryRunMessage="
        + dryRunMessage
        + ", excludeMergeCommits="
        + excludeMergeCommits
        + ", excludeTagCommits="
        + excludeTagCommits
        + ", groups="
        + groups
        + ", rejectMessage="
        + rejectMessage
        + ", requireMatchingAuthorEmail="
        + requireMatchingAuthorEmail
        + ", requireMatchingCommitterEmail="
        + requireMatchingCommitterEmail
        + ", requireMatchingAuthorEmailMessage="
        + requireMatchingAuthorEmailMessage
        + ", requireMatchingAuthorName="
        + requireMatchingAuthorName
        + ", requireMatchingAuthorNameSlug="
        + requireMatchingAuthorNameSlug
        + ", requireMatchingCommitterName="
        + requireMatchingCommitterName
        + ", requireMatchingCommitterNameSlug="
        + requireMatchingCommitterNameSlug
        + ", requireMatchingAuthorNameMessage="
        + requireMatchingAuthorNameMessage
        + ", requireOnlyOneIssue="
        + requireOnlyOneIssue
        + ", requireOnlyOneIssueMessage="
        + requireOnlyOneIssueMessage
        + ", branchRejectionRegexp="
        + branchRejectionRegexp
        + ", branchRejectionRegexpMessage="
        + branchRejectionRegexpMessage
        + ", allowServiceUsers="
        + allowServiceUsers
        + ", requireMatchingAuthorEmailRegexp="
        + requireMatchingAuthorEmailRegexp
        + ", jqlCheck="
        + jqlCheck
        + ", jqlCheckMessage="
        + jqlCheckMessage
        + ", jqlCheckQuery="
        + jqlCheckQuery
        + ", commitRegexp="
        + commitRegexp
        + ", requireMatchingAuthorEmailInBitbucket="
        + requireMatchingAuthorEmailInBitbucket
        + ", requireMatchingAuthorNameInBitbucket="
        + requireMatchingAuthorNameInBitbucket
        + ", requireMatchingAuthorNameInBitbucketSlug="
        + requireMatchingAuthorNameInBitbucketSlug
        + ", shouldCheckPullRequests="
        + shouldCheckPullRequests
        + ", shouldCheckPullRequestsMessage="
        + shouldCheckPullRequestsMessage
        + ", ignoreUsersPattern="
        + ignoreUsersPattern
        + "]";
  }
}
