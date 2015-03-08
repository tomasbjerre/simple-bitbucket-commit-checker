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
import static se.bjurr.sscc.settings.SSCCSettings.sscSettings;

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
import com.atlassian.stash.hook.repository.PreReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.google.common.annotations.VisibleForTesting;

public class SsccPreReceiveRepositoryHook implements PreReceiveRepositoryHook {
 private static final Logger logger = LoggerFactory.getLogger(PreReceiveRepositoryHook.class);

 private ChangeSetsService changesetsService;

 private String hookNameVersion;

 private final StashAuthenticationContext stashAuthenticationContext;

 public SsccPreReceiveRepositoryHook(ChangeSetsService changesetsService,
   StashAuthenticationContext stashAuthenticationContext) {
  this.hookNameVersion = "Simple Stash Commit Checker 1.1";
  this.changesetsService = changesetsService;
  this.stashAuthenticationContext = stashAuthenticationContext;
 }

 @VisibleForTesting
 public String getHookNameVersion() {
  return hookNameVersion;
 }

 @Override
 public boolean onReceive(RepositoryHookContext repositoryHookContext, Collection<RefChange> refChanges,
   HookResponse hookResponse) {
  try {
   SSCCRenderer ssccRenderer = new SSCCRenderer(this.stashAuthenticationContext, hookResponse);

   if (!hookNameVersion.isEmpty()) {
    ssccRenderer.println(hookNameVersion);
    ssccRenderer.println();
   }

   final SSCCSettings settings = sscSettings(repositoryHookContext.getSettings());
   final SSCCVerificationResult refChangeVerificationResults = validateRefChanges(repositoryHookContext, refChanges,
     settings, hookResponse);

   new SSCCPrinter(settings, ssccRenderer).printVerificationResults(refChanges, refChangeVerificationResults);

   if (settings.isDryRun() && settings.getDryRunMessage().isPresent()) {
    ssccRenderer.println(settings.getDryRunMessage().get());
   }

   if (!settings.isDryRun()) {
    return refChangeVerificationResults.isAccepted();
   }

   return TRUE;
  } catch (final Exception e) {
   final String message = "Error while validating reference changes. Will allow all of them. \"" + e.getMessage()
     + "\"";
   logger.error(message, e);
   hookResponse.out().println(message);
   return TRUE;
  }
 }

 @VisibleForTesting
 public void setChangesetsService(ChangeSetsService changesetsService) {
  this.changesetsService = changesetsService;
 }

 @VisibleForTesting
 public void setHookNameVersion(String hookNameVersion) {
  this.hookNameVersion = hookNameVersion;
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

 private SSCCVerificationResult validateRefChanges(RepositoryHookContext repositoryHookContext,
   Collection<RefChange> refChanges, SSCCSettings settings, HookResponse hookResponse) throws IOException {
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
}
