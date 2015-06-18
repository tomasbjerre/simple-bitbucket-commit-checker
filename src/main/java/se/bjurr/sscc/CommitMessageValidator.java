package se.bjurr.sscc;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newTreeMap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.regex.Pattern.compile;
import static se.bjurr.sscc.SSCCCommon.getStashEmail;
import static se.bjurr.sscc.SSCCCommon.getStashName;
import static se.bjurr.sscc.settings.SSCCGroup.Accept.ACCEPT;
import static se.bjurr.sscc.settings.SSCCGroup.Accept.SHOW_MESSAGE;
import static se.bjurr.sscc.settings.SSCCGroup.Match.ALL;
import static se.bjurr.sscc.settings.SSCCGroup.Match.NONE;
import static se.bjurr.sscc.settings.SSCCGroup.Match.ONE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCMatch;
import se.bjurr.sscc.settings.SSCCRule;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.stash.user.StashAuthenticationContext;

public class CommitMessageValidator {

 private final StashAuthenticationContext stashAuthenticationContext;
 private final SsccUserAdminService ssccUserAdminService;

 public CommitMessageValidator(StashAuthenticationContext stashAuthenticationContext,
   SsccUserAdminService ssccUserAdminService) {
  this.stashAuthenticationContext = stashAuthenticationContext;
  this.ssccUserAdminService = ssccUserAdminService;
 }

 public Map<SSCCGroup, SSCCMatch> validateChangeSetForGroups(SSCCSettings settings, final SSCCChangeSet ssccChangeSet) {
  final Map<SSCCGroup, SSCCMatch> allMatching = newTreeMap();
  for (final SSCCGroup group : settings.getGroups()) {
   final List<SSCCRule> matchingRules = newArrayList();
   for (final SSCCRule rule : group.getRules()) {
    if (compile(rule.getRegexp()).matcher(ssccChangeSet.getMessage()).find()) {
     matchingRules.add(rule);
    }
   }

   if (group.getAccept().equals(SHOW_MESSAGE)) {
    if (group.getMatch().equals(ALL) && matchingRules.size() == group.getRules().size()) {
     allMatching.put(group, new SSCCMatch(ALL, matchingRules));
    } else if (group.getMatch().equals(NONE) && matchingRules.isEmpty()) {
     allMatching.put(group, new SSCCMatch(NONE, matchingRules));
    } else if (group.getMatch().equals(ONE) && matchingRules.size() >= 1) {
     allMatching.put(group, new SSCCMatch(ONE, matchingRules));
    }
   }

   if (group.getAccept().equals(ACCEPT)) {
    if (group.getMatch().equals(ALL) && matchingRules.size() != group.getRules().size()) {
     allMatching.put(group, new SSCCMatch(ALL, matchingRules));
    } else if (group.getMatch().equals(NONE) && !matchingRules.isEmpty()) {
     allMatching.put(group, new SSCCMatch(NONE, matchingRules));
    } else if (group.getMatch().equals(ONE) && matchingRules.size() == 0) {
     allMatching.put(group, new SSCCMatch(ONE, matchingRules));
    }
   }
  }
  return allMatching;
 }

 public boolean validateChangeSetForCommitterEmail(SSCCSettings settings, SSCCChangeSet ssccChangeSet,
   SSCCRenderer ssccRenderer) {
  if (settings.shouldRequireMatchingCommitterEmail()) {
   if (settings.getRequireMatchingAuthorEmailRegexp().isPresent()) {
    return compile(ssccRenderer.render(settings.getRequireMatchingAuthorEmailRegexp().get())).matcher(
      ssccChangeSet.getCommitter().getEmailAddress()).find();
   }
   return getStashEmail(stashAuthenticationContext).equals(ssccChangeSet.getCommitter().getEmailAddress());
  }
  return TRUE;
 }

 public boolean validateChangeSetForAuthorEmail(SSCCSettings settings, SSCCChangeSet ssccChangeSet,
   SSCCRenderer ssccRenderer) {
  if (settings.shouldRequireMatchingAuthorEmail()) {
   if (settings.getRequireMatchingAuthorEmailRegexp().isPresent()) {
    return compile(ssccRenderer.render(settings.getRequireMatchingAuthorEmailRegexp().get())).matcher(
      ssccChangeSet.getAuthor().getEmailAddress()).find();
   }
   return getStashEmail(stashAuthenticationContext).equals(ssccChangeSet.getAuthor().getEmailAddress());
  }
  return TRUE;
 }

 public boolean validateChangeSetForAuthorName(SSCCSettings settings, SSCCChangeSet ssccChangeSet) {
  if (settings.shouldRequireMatchingAuthorName()
    && !getStashName(stashAuthenticationContext).equals(ssccChangeSet.getAuthor().getName())) {
   return FALSE;
  }
  return TRUE;
 }

 public boolean validateChangeSetForCommitterName(SSCCSettings settings, SSCCChangeSet ssccChangeSet)
   throws ExecutionException {
  if (settings.shouldRequireMatchingCommitterName()
    && !getStashName(stashAuthenticationContext).equals(ssccChangeSet.getCommitter().getName())) {
   return FALSE;
  }
  return TRUE;
 }

 public boolean validateChangeSetForAuthorEmailInStash(SSCCSettings settings, SSCCChangeSet ssccChangeSet)
   throws ExecutionException {
  return !settings.getRequireMatchingAuthorEmailInStash() || settings.getRequireMatchingAuthorEmailInStash()
    && ssccUserAdminService.getStashUsers().containsKey(ssccChangeSet.getAuthor().getEmailAddress());
 }

 public boolean validateChangeSetForAuthorNameInStash(SSCCSettings settings, SSCCChangeSet ssccChangeSet)
   throws ExecutionException {
  return !settings.getRequireMatchingAuthorNameInStash() || settings.getRequireMatchingAuthorNameInStash()
    && ssccUserAdminService.getStashUsers().containsKey(ssccChangeSet.getAuthor().getName());
 }

}