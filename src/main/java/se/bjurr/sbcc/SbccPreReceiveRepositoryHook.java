package se.bjurr.sbcc;

import static com.google.common.collect.Lists.newArrayList;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.hook.ScmHookDetails;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookRequest;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.SecurityService;
import java.util.List;
import se.bjurr.sbcc.commits.ChangeSetsService;

public class SbccPreReceiveRepositoryHook implements PreRepositoryHook<RepositoryHookRequest> {

  private final SbccRepositoryHook repositoryHook;

  public SbccPreReceiveRepositoryHook(
      ChangeSetsService changesetsService,
      AuthenticationContext bitbucketAuthenticationContext,
      ApplicationLinkService applicationLinkService,
      SbccUserAdminService sbccUserAdminService,
      SecurityService securityService,
      RepositoryHookService repositoryHookService) {
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
      PreRepositoryHookContext context, RepositoryHookRequest request) {
    List<RefChange> refChanges = newArrayList(request.getRefChanges());
    ScmHookDetails scmHookDetails = request.getScmHookDetails().orElse(null);
    Repository repository = request.getRepository();
    return repositoryHook.performChecks(refChanges, scmHookDetails, repository);
  }
}
