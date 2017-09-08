package se.bjurr.sbcc;

import static com.atlassian.bitbucket.hook.repository.RepositoryHookResult.accepted;
import static com.atlassian.bitbucket.hook.repository.RepositoryHookResult.rejected;
import static com.atlassian.bitbucket.permission.Permission.REPO_ADMIN;
import static java.util.Optional.empty;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static se.bjurr.sbcc.commits.ChangeSetsService.isTag;
import static se.bjurr.sbcc.settings.SbccSettings.sscSettings;

import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import se.bjurr.sbcc.commits.ChangeSetsService;
import se.bjurr.sbcc.data.SbccVerificationResult;
import se.bjurr.sbcc.settings.SbccSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.hook.ScmHookDetails;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.Operation;
import com.google.common.annotations.VisibleForTesting;

public class SbccRepositoryHook {
  public static final String PR_REJECT_DEFAULT_MSG = "At least one commit is not ok";
  private static final String HOOK_SETTINGS_KEY = "se.bjurr.sscc.sscc:pre-receive-repository-hook";
  private static Logger logger = Logger.getLogger(SbccRepositoryHook.class.getName());

  @VisibleForTesting
  public static Logger getLogger() {
    return logger;
  }

  @VisibleForTesting
  public static void setLogger(Logger logger) {
    SbccRepositoryHook.logger = logger;
  }

  private final ApplicationLinkService applicationLinkService;

  private final AuthenticationContext bitbucketAuthenticationContext;

  private ChangeSetsService changesetsService;

  private String hookName;

  private final SbccUserAdminService sbccUserAdminService;

  private final SecurityService securityService;

  private final RepositoryHookService repositoryHookService;

  public SbccRepositoryHook(
      ChangeSetsService changesetsService,
      AuthenticationContext bitbucketAuthenticationContext,
      ApplicationLinkService applicationLinkService,
      SbccUserAdminService sbccUserAdminService,
      SecurityService securityService,
      RepositoryHookService repositoryHookService) {
    this.hookName = "Simple Bitbucket Commit Checker";
    this.changesetsService = changesetsService;
    this.bitbucketAuthenticationContext = bitbucketAuthenticationContext;
    this.applicationLinkService = applicationLinkService;
    this.sbccUserAdminService = sbccUserAdminService;
    this.securityService = securityService;
    this.repositoryHookService = repositoryHookService;
  }

  public RepositoryHookResult performChecks(
      List<RefChange> refChanges, ScmHookDetails scmHookDetails, Repository repository) {

    PrintWriter responseWriter = null;
    if (scmHookDetails != null) {
      responseWriter = scmHookDetails.out();
      if (!this.hookName.isEmpty()) {
        responseWriter.println(this.hookName + "\n");
      }
    }
    try {

      final StringBuilder hookResponse = new StringBuilder();
      final SbccRenderer sbccRenderer = new SbccRenderer(this.bitbucketAuthenticationContext);

      final Optional<Settings> rawSettingsOpt = findSettings(repository);
      if (!rawSettingsOpt.isPresent()) {
        return acceptedResponse(responseWriter, hookResponse);
      }
      final SbccSettings settings =
          sscSettings(new RenderingSettings(rawSettingsOpt.get(), sbccRenderer));

      if (new UserValidator(settings, this.bitbucketAuthenticationContext.getCurrentUser())
          .shouldIgnoreChecksForUser()) {
        return accepted();
      }

      final RefChangeValidator refChangeValidator =
          new RefChangeValidator(
              repository,
              settings,
              this.changesetsService,
              this.bitbucketAuthenticationContext,
              sbccRenderer,
              this.applicationLinkService,
              this.sbccUserAdminService);

      final SbccVerificationResult refChangeVerificationResults = new SbccVerificationResult();
      for (final RefChange refChange : refChanges) {
        final boolean isTag = isTag(refChange.getRef().getId());
        final Boolean shouldExcludeTagCommits = settings.shouldExcludeTagCommits();
        if (isTag && shouldExcludeTagCommits) {
          continue;
        }
        refChangeValidator.validateRefChange(
            refChangeVerificationResults,
            refChange.getType(),
            refChange.getRef().getId(),
            refChange.getFromHash(),
            refChange.getToHash());
      }

      final String printOut =
          new SbccPrinter(settings, sbccRenderer)
              .printVerificationResults(refChangeVerificationResults);

      hookResponse.append(printOut);
      if (settings.isDryRun() && settings.getDryRunMessage().isPresent()) {
        hookResponse.append(settings.getDryRunMessage().get());
      }

      if (!settings.isDryRun()) {
        if (refChangeVerificationResults.isAccepted()) {
          return acceptedResponse(responseWriter, hookResponse);
        } else {
          final String summary =
              settings
                  .getShouldCheckPullRequestsMessage() //
                  .or(PR_REJECT_DEFAULT_MSG);
          return rejected(summary, hookResponse.toString());
        }
      }
      return acceptedResponse(responseWriter, hookResponse);
    } catch (final Exception e) {
      final String message =
          "Error while validating reference changes. Will allow all of them. \""
              + e.getMessage()
              + "\"";
      logger.log(SEVERE, message, e);
      if (responseWriter != null) {
        responseWriter.println(message);
      }
      return accepted();
    }
  }

  private RepositoryHookResult acceptedResponse(
      PrintWriter responseWriter, StringBuilder hookResponse) {
    if (responseWriter != null) {
      responseWriter.print(hookResponse.toString());
    }
    logger.log(INFO, "Accepting\n" + hookResponse.toString());
    return accepted();
  }

  public Optional<Settings> findSettings(final Repository repository) {
    try {
      final Settings settings =
          this.securityService
              .withPermission(REPO_ADMIN, "Retrieving settings")
              .call(
                  new Operation<Settings, Exception>() {
                    @Override
                    public Settings perform() throws Exception {
                      final RepositoryHook hook =
                          repositoryHookService.getByKey(repository, HOOK_SETTINGS_KEY);
                      if (!hook.isEnabled() || !hook.isEnabled()) {
                        return null;
                      }
                      return repositoryHookService.getSettings(repository, HOOK_SETTINGS_KEY);
                    }
                  });
      if (settings == null) {
        return empty();
      }
      final SbccRenderer sbccRenderer = new SbccRenderer(this.bitbucketAuthenticationContext);
      final SbccSettings abccSettings =
          SbccSettings.sscSettings(new RenderingSettings(settings, sbccRenderer));
      logger.log(INFO, "Using settings:\n" + abccSettings);
      return Optional.of(settings);
    } catch (final Exception e) {
      logger.log(SEVERE, "Tried to get settings for \"" + HOOK_SETTINGS_KEY + "\"", e);
      return empty();
    }
  }

  @VisibleForTesting
  public String getHookName() {
    return this.hookName;
  }

  @VisibleForTesting
  public void setChangesetsService(ChangeSetsService changesetsService) {
    this.changesetsService = changesetsService;
  }

  @VisibleForTesting
  public void setHookName(String hookName) {
    this.hookName = hookName;
  }
}
