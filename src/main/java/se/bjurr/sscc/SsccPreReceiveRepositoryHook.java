package se.bjurr.sscc;

import static com.atlassian.stash.user.UserType.SERVICE;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.settings.SSCCSettings.sscSettings;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.sscc.data.SSCCVerificationResult;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.PreReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.google.common.annotations.VisibleForTesting;

public class SsccPreReceiveRepositoryHook implements PreReceiveRepositoryHook {
 private static Logger logger = LoggerFactory.getLogger(PreReceiveRepositoryHook.class);

 private ChangeSetsService changesetsService;

 private String hookName;

 private final StashAuthenticationContext stashAuthenticationContext;

 private final ApplicationLinkService applicationLinkService;

 public SsccPreReceiveRepositoryHook(ChangeSetsService changesetsService,
   StashAuthenticationContext stashAuthenticationContext, ApplicationLinkService applicationLinkService) {
  this.hookName = "Simple Stash Commit Checker";
  this.changesetsService = changesetsService;
  this.stashAuthenticationContext = stashAuthenticationContext;
  this.applicationLinkService = applicationLinkService;
 }

 @VisibleForTesting
 public String getHookName() {
  return hookName;
 }

 @Override
 public boolean onReceive(RepositoryHookContext repositoryHookContext, Collection<RefChange> refChanges,
   HookResponse hookResponse) {
  try {
   SSCCRenderer ssccRenderer = new SSCCRenderer(this.stashAuthenticationContext, hookResponse);

   if (!hookName.isEmpty()) {
    ssccRenderer.println(hookName);
    ssccRenderer.println();
   }

   final SSCCSettings settings = sscSettings(repositoryHookContext.getSettings());
   final SSCCVerificationResult refChangeVerificationResults = new RefChangeValidator(repositoryHookContext,
     refChanges, settings, hookResponse, changesetsService, stashAuthenticationContext, ssccRenderer,
     applicationLinkService).validateRefChanges();

   new SSCCPrinter(settings, ssccRenderer).printVerificationResults(refChanges, refChangeVerificationResults);

   if (settings.isDryRun() && settings.getDryRunMessage().isPresent()) {
    ssccRenderer.println(settings.getDryRunMessage().get());
   }

   if (!settings.isDryRun()
     && !(settings.allowServiceUsers() && stashAuthenticationContext.getCurrentUser().getType().equals(SERVICE))) {
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
 public void setHookName(String hookName) {
  this.hookName = hookName;
 }

 @VisibleForTesting
 public static Logger getLogger() {
  return logger;
 }

 @VisibleForTesting
 public static void setLogger(Logger logger) {
  SsccPreReceiveRepositoryHook.logger = logger;
 }
}
