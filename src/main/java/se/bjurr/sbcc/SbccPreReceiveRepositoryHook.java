package se.bjurr.sbcc;

import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.SEVERE;
import static se.bjurr.sbcc.settings.SbccSettings.sscSettings;

import java.util.Collection;
import java.util.logging.Logger;

import se.bjurr.sbcc.data.SbccVerificationResult;
import se.bjurr.sbcc.settings.SbccSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.hook.HookResponse;
import com.atlassian.bitbucket.hook.repository.PreReceiveRepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.repository.RefChange;
import com.google.common.annotations.VisibleForTesting;

public class SbccPreReceiveRepositoryHook implements PreReceiveRepositoryHook {
 private static Logger logger = Logger.getLogger(SbccPreReceiveRepositoryHook.class.getName());

 private ChangeSetsService changesetsService;

 private String hookName;

 private final AuthenticationContext bitbucketAuthenticationContext;

 private final ApplicationLinkService applicationLinkService;

 private final SbccUserAdminService sbccUserAdminService;

 public SbccPreReceiveRepositoryHook(ChangeSetsService changesetsService,
   AuthenticationContext bitbucketAuthenticationContext, ApplicationLinkService applicationLinkService,
   SbccUserAdminService sbccUserAdminService) {
  this.hookName = "Simple Bitbucket Commit Checker";
  this.changesetsService = changesetsService;
  this.bitbucketAuthenticationContext = bitbucketAuthenticationContext;
  this.applicationLinkService = applicationLinkService;
  this.sbccUserAdminService = sbccUserAdminService;
 }

 @VisibleForTesting
 public String getHookName() {
  return hookName;
 }

 @Override
 public boolean onReceive(RepositoryHookContext repositoryHookContext, Collection<RefChange> refChanges,
   HookResponse hookResponse) {
  try {
   if (!hookName.isEmpty()) {
    hookResponse.out().println(hookName);
    hookResponse.out().println();
   }
   final SbccSettings settings = sscSettings(repositoryHookContext.getSettings());

   if (new UserValidator(settings, bitbucketAuthenticationContext.getCurrentUser()).shouldIgnoreChecksForUser()) {
    return TRUE;
   }

   SbccRenderer sbccRenderer = new SbccRenderer(this.bitbucketAuthenticationContext);
   final SbccVerificationResult refChangeVerificationResults = new RefChangeValidator(
     repositoryHookContext.getRepository(), repositoryHookContext.getRepository(), settings, changesetsService,
     bitbucketAuthenticationContext, sbccRenderer, applicationLinkService, sbccUserAdminService)
     .validateRefChanges(refChanges);

   String printOut = new SbccPrinter(settings, sbccRenderer).printVerificationResults(refChangeVerificationResults);

   hookResponse.out().print(printOut);
   if (settings.isDryRun() && settings.getDryRunMessage().isPresent()) {
    hookResponse.out().println(settings.getDryRunMessage().get());
   }

   if (!settings.isDryRun()) {
    return refChangeVerificationResults.isAccepted();
   }

   return TRUE;
  } catch (final Exception e) {
   final String message = "Error while validating reference changes. Will allow all of them. \"" + e.getMessage()
     + "\"";
   logger.log(SEVERE, message, e);
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
  SbccPreReceiveRepositoryHook.logger = logger;
 }
}
