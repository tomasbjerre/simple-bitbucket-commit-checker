package se.bjurr.sscc;

import static com.atlassian.stash.repository.RefChangeType.DELETE;
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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.data.SSCCRefChangeVerificationResult;
import se.bjurr.sscc.data.SSCCVerificationResult;
import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCMatch;
import se.bjurr.sscc.settings.SSCCRule;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.user.StashAuthenticationContext;

public class RefChangeValidator {
 private static Logger logger = LoggerFactory.getLogger(RefChangeValidator.class);

 private final RepositoryHookContext repositoryHookContext;
 private final Collection<RefChange> refChanges;
 private final SSCCSettings settings;
 private final HookResponse hookResponse;
 private final ChangeSetsService changesetsService;
 private final StashAuthenticationContext stashAuthenticationContext;

 public RefChangeValidator(RepositoryHookContext repositoryHookContext, Collection<RefChange> refChanges,
   SSCCSettings settings, HookResponse hookResponse, ChangeSetsService changesetsService,
   StashAuthenticationContext stashAuthenticationContext) {
  this.repositoryHookContext = repositoryHookContext;
  this.refChanges = refChanges;
  this.settings = settings;
  this.hookResponse = hookResponse;
  this.changesetsService = changesetsService;
  this.stashAuthenticationContext = stashAuthenticationContext;
 }

 public SSCCVerificationResult validateRefChanges() throws IOException {
  final SSCCVerificationResult refChangeVerificationResult = new SSCCVerificationResult();
  for (final RefChange refChange : refChanges) {
   logger.info(getStashName(stashAuthenticationContext) + " " + getStashEmail(stashAuthenticationContext)
     + "> RefChange " + refChange.getFromHash() + " " + refChange.getRefId() + " " + refChange.getToHash() + " "
     + refChange.getType());
   if (compile(settings.getBranches().or(".*")).matcher(refChange.getRefId()).find()) {
    if (refChange.getType() != DELETE) {
     List<SSCCChangeSet> refChangeSets = changesetsService.getNewChangeSets(settings,
       repositoryHookContext.getRepository(), refChange);
     final SSCCRefChangeVerificationResult refChangeVerificationResults = validateRefChange(refChange, refChangeSets,
       settings, hookResponse);
     if (!refChangeVerificationResults.isEmpty()) {
      refChangeVerificationResult.add(refChangeVerificationResults);
     }
    }
   }
  }
  return refChangeVerificationResult;
 }

 private boolean validateChangeSetForEmail(SSCCSettings settings, SSCCChangeSet ssccChangeSet) {
  if (!settings.shouldRequireMatchingAuthorEmail()) {
   return TRUE;
  }
  if (getStashEmail(stashAuthenticationContext).equals(ssccChangeSet.getCommitter().getEmailAddress())) {
   return TRUE;
  }
  return FALSE;
 }

 private Map<SSCCGroup, SSCCMatch> validateChangeSetForGroups(SSCCSettings settings, final SSCCChangeSet ssccChangeSet) {
  final Map<SSCCGroup, SSCCMatch> allMatching = newTreeMap();
  if (ssccChangeSet.getParentCount() > 1 && settings.shouldExcludeMergeCommits()) {
   return allMatching;
  }
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

 private boolean validateChangeSetForName(SSCCSettings settings, SSCCChangeSet ssccChangeSet) {
  if (!settings.shouldRequireMatchingAuthorName()) {
   return TRUE;
  }
  if (getStashName(stashAuthenticationContext).equals(ssccChangeSet.getCommitter().getName())) {
   return TRUE;
  }
  return FALSE;
 }

 private SSCCRefChangeVerificationResult validateRefChange(RefChange refChange, List<SSCCChangeSet> ssccChangeSets,
   SSCCSettings settings, HookResponse hookResponse) throws IOException {
  final SSCCRefChangeVerificationResult refChangeVerificationResult = new SSCCRefChangeVerificationResult(refChange);
  for (final SSCCChangeSet ssccChangeSet : ssccChangeSets) {
   logger.info(getStashName(stashAuthenticationContext) + " " + getStashEmail(stashAuthenticationContext)
     + "> ChangeSet " + ssccChangeSet.getId() + " " + ssccChangeSet.getMessage() + " " + ssccChangeSet.getParentCount()
     + " " + ssccChangeSet.getCommitter().getEmailAddress() + " " + ssccChangeSet.getCommitter().getName());
   refChangeVerificationResult.setGroupsResult(ssccChangeSet, validateChangeSetForGroups(settings, ssccChangeSet));
   refChangeVerificationResult.addEmailValidationResult(ssccChangeSet,
     validateChangeSetForEmail(settings, ssccChangeSet));
   refChangeVerificationResult
     .addNameValidationResult(ssccChangeSet, validateChangeSetForName(settings, ssccChangeSet));
  }
  return refChangeVerificationResult;
 }
}
