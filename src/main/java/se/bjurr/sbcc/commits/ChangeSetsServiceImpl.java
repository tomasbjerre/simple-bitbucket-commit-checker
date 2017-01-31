package se.bjurr.sbcc.commits;

import static com.atlassian.bitbucket.repository.RefChangeType.DELETE;
import static com.atlassian.bitbucket.scm.git.GitRefPattern.TAGS;
import static com.google.common.collect.Lists.newArrayList;
import static se.bjurr.sbcc.commits.RevListOutputHandler.FORMAT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.RefService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scm.ScmService;
import com.atlassian.bitbucket.scm.git.GitScm;
import com.atlassian.bitbucket.scm.git.command.GitScmCommandBuilder;
import com.atlassian.bitbucket.scm.git.command.revlist.GitRevListBuilder;
import com.google.common.base.Optional;

import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.settings.SbccSettings;

public class ChangeSetsServiceImpl implements ChangeSetsService {
  private static Logger logger = LoggerFactory.getLogger(ChangeSetsServiceImpl.class.getName());

  private final ScmService scmService;

  public ChangeSetsServiceImpl(RefService refService, ScmService scmService) {
    this.scmService = scmService;
  }

  @Override
  public List<SbccChangeSet> getNewChangeSets(SbccSettings settings, PullRequest pullRequest)
      throws IOException {
    return getNewChangesets(
        settings,
        pullRequest.getToRef().getRepository(),
        pullRequest.getToRef().getId(),
        RefChangeType.ADD,
        pullRequest.getToRef().getLatestCommit());
  }

  @Override
  public List<SbccChangeSet> getNewChangeSets(
      SbccSettings settings,
      Repository repository,
      String refId,
      RefChangeType type,
      String fromHash,
      String toHash)
      throws IOException {
    return getNewChangesets(settings, repository, refId, type, toHash);
  }

  private Optional<GitScmCommandBuilder> findGitScmCommandBuilder(Repository repository) {
    if (!GitScm.ID.equals(repository.getScmId())) {
      logger.warn("SCM " + repository.getScmId() + " not supported");
      return Optional.absent();
    }
    return Optional.of((GitScmCommandBuilder) scmService.createBuilder(repository));
  }

  private List<SbccChangeSet> getNewChangesets(
      SbccSettings settings,
      Repository repository,
      String refId,
      RefChangeType type,
      String toHash) {

    Optional<GitScmCommandBuilder> gitScmCommandBuilder = findGitScmCommandBuilder(repository);
    if (!gitScmCommandBuilder.isPresent()) {
      return newArrayList();
    }

    if (refId.startsWith(TAGS.getPath())) {
      return getTag(type, toHash, gitScmCommandBuilder);
    } else {
      return getCommits(toHash, gitScmCommandBuilder, settings);
    }
  }

  private List<SbccChangeSet> getCommits(
      String toHash, Optional<GitScmCommandBuilder> gitScmCommandBuilder, SbccSettings settings) {
    GitRevListBuilder revListBuilder =
        gitScmCommandBuilder
            .get() //
            .revList() //
            .format(FORMAT) //
            .revs(toHash, "--not", "--all");

    List<SbccChangeSet> found =
        revListBuilder //
            .build(new RevListOutputHandler(settings)) //
            .call();

    if (found != null) {
      return found;
    }
    return newArrayList();
  }

  private List<SbccChangeSet> getTag(
      RefChangeType type, String toHash, Optional<GitScmCommandBuilder> gitScmCommandBuilder) {
    if (type == DELETE) {
      return new ArrayList<>();
    }

    SbccChangeSet sbccChangeSet =
        gitScmCommandBuilder
            .get() //
            .catFile() //
            .pretty() //
            .object(toHash) //
            .build(new AnnotatedTagOutputHandler(toHash)) //
            .call();

    if (sbccChangeSet != null) {
      newArrayList(sbccChangeSet);
    }

    return newArrayList();
  }
}
