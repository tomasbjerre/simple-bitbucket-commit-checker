package se.bjurr.sbcc;

import static com.atlassian.bitbucket.hook.repository.RepositoryHookResult.accepted;
import static com.atlassian.bitbucket.repository.RefChangeType.ADD;
import static java.util.logging.Level.SEVERE;
import static se.bjurr.sbcc.settings.SbccSettings.sscSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import se.bjurr.sbcc.commits.ChangeSetsService;
import se.bjurr.sbcc.settings.ValidationException;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.hook.ScmHookDetails;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.PullRequestMergeHookRequest;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.hook.repository.RepositoryMergeCheck;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.SecurityService;

public class SbccRepositoryMergeCheck implements RepositoryMergeCheck {
  private static Logger logger = Logger.getLogger(SbccRepositoryMergeCheck.class.getName());

  private final SbccRepositoryHook repositoryHook;

  public SbccRepositoryMergeCheck(
      ChangeSetsService changesetsService,
      AuthenticationContext bitbucketAuthenticationContext,
      ApplicationLinkService applicationLinkService,
      SbccUserAdminService sbccUserAdminService,
      SecurityService securityService,
      RepositoryHookService repositoryHookService,
      ChangeSetsService shangeSetsService) {
    this.repositoryHook =
        new SbccRepositoryHook(
            changesetsService,
            bitbucketAuthenticationContext,
            applicationLinkService,
            sbccUserAdminService,
            securityService,
            repositoryHookService);
  }

  @Override
  public RepositoryHookResult preUpdate(
      PreRepositoryHookContext context, PullRequestMergeHookRequest request) {
    final PullRequest pullRequest = request.getPullRequest();
    final ScmHookDetails scmHookDetails = request.getScmHookDetails().orElse(null);
    final Repository repositoryWithCommits = request.getFromRef().getRepository();
    final Repository repositoryWithSettings = request.getToRef().getRepository();
    final Optional<Settings> settings = repositoryHook.findSettings(repositoryWithSettings);

    if (!settings.isPresent()) {
      return accepted();
    }

    try {
      final boolean shouldCheckPr =
          sscSettings(settings.get()) //
              .shouldCheckPullRequests();
      if (!shouldCheckPr) {
        return accepted();
      }
    } catch (final ValidationException e) {
      logger.log(SEVERE, "Could not read settings", e);
      return accepted();
    }
    final List<RefChange> refChanges = new ArrayList<>();
    final RefChange refChange =
        new RefChange() {
          @Override
          public RefChangeType getType() {
            return ADD;
          }

          @Override
          public String getToHash() {
            return pullRequest.getFromRef().getLatestCommit();
          }

          @Override
          public MinimalRef getRef() {
            return pullRequest.getFromRef();
          }

          @Override
          public String getFromHash() {
            return pullRequest.getToRef().getLatestCommit();
          }
        };
    refChanges.add(refChange);
    return repositoryHook.performChecks(refChanges, scmHookDetails, repositoryWithCommits);
  }
}
