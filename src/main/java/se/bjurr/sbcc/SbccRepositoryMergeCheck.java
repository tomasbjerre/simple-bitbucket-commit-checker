package se.bjurr.sbcc;

import static com.atlassian.bitbucket.hook.repository.RepositoryHookResult.accepted;
import static com.atlassian.bitbucket.repository.RefChangeType.ADD;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import se.bjurr.sbcc.commits.ChangeSetsService;

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
    PullRequest pullRequest = request.getPullRequest();
    ScmHookDetails scmHookDetails = request.getScmHookDetails().orElse(null);
    Repository repositoryWithCommits = request.getFromRef().getRepository();
    Repository repositoryWithSettings = request.getToRef().getRepository();
    Optional<Settings> settings = repositoryHook.findSettings(repositoryWithSettings);

    if (settings.isPresent()) {
      List<RefChange> refChanges = new ArrayList<>();
      RefChange refChange =
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
    return accepted();
  }
}
