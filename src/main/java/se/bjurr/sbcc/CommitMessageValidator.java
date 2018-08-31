package se.bjurr.sbcc;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newTreeMap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.regex.Pattern.compile;
import static se.bjurr.sbcc.SbccCommon.getBitbucketEmail;
import static se.bjurr.sbcc.SbccCommon.getBitbucketName;
import static se.bjurr.sbcc.SbccCommon.getBitbucketSlug;
import static se.bjurr.sbcc.settings.SbccGroup.Accept.ACCEPT;
import static se.bjurr.sbcc.settings.SbccGroup.Accept.SHOW_MESSAGE;
import static se.bjurr.sbcc.settings.SbccGroup.Match.ALL;
import static se.bjurr.sbcc.settings.SbccGroup.Match.NONE;
import static se.bjurr.sbcc.settings.SbccGroup.Match.ONE;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.settings.SbccGroup;
import se.bjurr.sbcc.settings.SbccMatch;
import se.bjurr.sbcc.settings.SbccRule;
import se.bjurr.sbcc.settings.SbccSettings;

public class CommitMessageValidator {

  private final AuthenticationContext bitbucketAuthenticationContext;
  private final SbccUserAdminService sbccUserAdminService;

  public CommitMessageValidator(
      AuthenticationContext bitbucketAuthenticationContext,
      SbccUserAdminService sbccUserAdminService) {
    this.bitbucketAuthenticationContext = bitbucketAuthenticationContext;
    this.sbccUserAdminService = sbccUserAdminService;
  }

  public boolean validateChangeSetForAuthorEmail(
      SbccSettings settings, SbccChangeSet sbccChangeSet, SbccRenderer sbccRenderer) {
    if (settings.getRequireMatchingAuthorEmailRegexp().isPresent()) {
      return compile(sbccRenderer.render(settings.getRequireMatchingAuthorEmailRegexp().get()))
          .matcher(sbccChangeSet.getAuthor().getEmailAddress())
          .find();
    }
    if (settings.shouldRequireMatchingAuthorEmail()) {
      return getBitbucketEmail(this.bitbucketAuthenticationContext) //
          .equalsIgnoreCase(sbccChangeSet.getAuthor().getEmailAddress());
    }
    return TRUE;
  }

  public boolean validateChangeSetForAuthorEmailInBitbucket(
      SbccSettings settings, SbccChangeSet sbccChangeSet) throws ExecutionException {
    if (settings.getRequireMatchingAuthorEmailInBitbucket()) {
      return this.sbccUserAdminService.emailExists(sbccChangeSet.getAuthor().getEmailAddress());
    } else {
      return TRUE;
    }
  }

  public boolean validateChangeSetForAuthorName(
      SbccSettings settings, SbccChangeSet sbccChangeSet) {
    if (settings.shouldRequireMatchingAuthorName()
        && !getBitbucketName(this.bitbucketAuthenticationContext)
            .equals(sbccChangeSet.getAuthor().getName())) {
      return FALSE;
    }
    if (settings.isRequireMatchingAuthorNameSlug()
        && !getBitbucketSlug(this.bitbucketAuthenticationContext)
            .equals(sbccChangeSet.getAuthor().getName())) {
      return FALSE;
    }
    return TRUE;
  }

  public boolean validateChangeSetForAuthorNameInBitbucket(
      SbccSettings settings, SbccChangeSet sbccChangeSet) throws ExecutionException {
    if (settings.getRequireMatchingAuthorNameInBitbucket()) {
      return this.sbccUserAdminService.displayNameExists(sbccChangeSet.getAuthor().getName());
    }
    if (settings.isRequireMatchingAuthorNameInBitbucketSlug()) {
      return this.sbccUserAdminService.slugExists(sbccChangeSet.getAuthor().getName());
    }
    return true;
  }

  public boolean validateChangeSetForCommitterEmail(
      SbccSettings settings, SbccChangeSet sbccChangeSet, SbccRenderer sbccRenderer) {
    if (settings.shouldRequireMatchingCommitterEmail()) {
      if (settings.getRequireMatchingAuthorEmailRegexp().isPresent()) {
        return compile(sbccRenderer.render(settings.getRequireMatchingAuthorEmailRegexp().get()))
            .matcher(sbccChangeSet.getCommitter().getEmailAddress())
            .find();
      }
      return getBitbucketEmail(this.bitbucketAuthenticationContext) //
          .equalsIgnoreCase(sbccChangeSet.getCommitter().getEmailAddress());
    }
    return TRUE;
  }

  public boolean validateChangeSetForCommitterName(
      SbccSettings settings, SbccChangeSet sbccChangeSet) throws ExecutionException {
    if (settings.shouldRequireMatchingCommitterName()
        && !getBitbucketName(this.bitbucketAuthenticationContext)
            .equals(sbccChangeSet.getCommitter().getName())) {
      return FALSE;
    }
    if (settings.isRequireMatchingCommitterNameSlug()
        && !getBitbucketSlug(this.bitbucketAuthenticationContext)
            .equals(sbccChangeSet.getCommitter().getName())) {
      return FALSE;
    }
    return TRUE;
  }

  public Map<SbccGroup, SbccMatch> validateChangeSetForGroups(
      SbccSettings settings, final SbccChangeSet sbccChangeSet) {
    final Map<SbccGroup, SbccMatch> allMatching = newTreeMap();
    for (final SbccGroup group : settings.getGroups()) {
      final List<SbccRule> matchingRules = newArrayList();
      for (final SbccRule rule : group.getRules()) {
        if (compile(rule.getRegexp()).matcher(sbccChangeSet.getMessage()).find()) {
          matchingRules.add(rule);
        }
      }

      if (group.getAccept().equals(SHOW_MESSAGE)) {
        if (group.getMatch().equals(ALL) && matchingRules.size() == group.getRules().size()) {
          allMatching.put(group, new SbccMatch(ALL, matchingRules));
        } else if (group.getMatch().equals(NONE) && matchingRules.isEmpty()) {
          allMatching.put(group, new SbccMatch(NONE, matchingRules));
        } else if (group.getMatch().equals(ONE) && matchingRules.size() >= 1) {
          allMatching.put(group, new SbccMatch(ONE, matchingRules));
        }
      }

      if (group.getAccept().equals(ACCEPT)) {
        if (group.getMatch().equals(ALL) && matchingRules.size() != group.getRules().size()) {
          allMatching.put(group, new SbccMatch(ALL, matchingRules));
        } else if (group.getMatch().equals(NONE) && !matchingRules.isEmpty()) {
          allMatching.put(group, new SbccMatch(NONE, matchingRules));
        } else if (group.getMatch().equals(ONE) && matchingRules.size() == 0) {
          allMatching.put(group, new SbccMatch(ONE, matchingRules));
        }
      }
    }
    return allMatching;
  }
}
