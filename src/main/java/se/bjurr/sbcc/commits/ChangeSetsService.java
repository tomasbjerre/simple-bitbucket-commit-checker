package se.bjurr.sbcc.commits;

import static com.atlassian.bitbucket.repository.RefChangeType.DELETE;
import static com.atlassian.bitbucket.scm.git.GitRefPattern.TAGS;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static se.bjurr.sbcc.commits.RevListOutputHandler.FORMAT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.settings.SbccSettings;

import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.RefService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scm.ScmService;
import com.atlassian.bitbucket.scm.git.GitScm;
import com.atlassian.bitbucket.scm.git.command.GitScmCommandBuilder;
import com.atlassian.bitbucket.scm.git.command.revlist.GitRevListBuilder;
import com.google.common.base.Optional;

public class ChangeSetsService {
  private static final String FIRST_COMMIT = "0000000000000000000000000000000000000000";
  private static Logger logger = getLogger(ChangeSetsService.class.getName());

  private final ScmService scmService;

  public ChangeSetsService(RefService refService, ScmService scmService) {
    this.scmService = scmService;
  }

  public List<SbccChangeSet> getNewChangeSets(
      SbccSettings settings,
      Repository repository,
      String refId,
      RefChangeType type,
      String fromHash,
      String toHash)
      throws IOException {
    return getNewChangesets(settings, repository, refId, type, fromHash, toHash);
  }

  private Optional<GitScmCommandBuilder> findGitScmCommandBuilder(Repository repository) {
    if (!GitScm.ID.equals(repository.getScmId())) {
      logger.log(WARNING, "SCM " + repository.getScmId() + " not supported");
      return Optional.absent();
    }
    return Optional.of((GitScmCommandBuilder) scmService.createBuilder(repository));
  }

  private List<SbccChangeSet> getNewChangesets(
      SbccSettings settings,
      Repository repository,
      String refId,
      RefChangeType type,
      String fromHash,
      String toHash) {

    final Optional<GitScmCommandBuilder> gitScmCommandBuilder =
        findGitScmCommandBuilder(repository);
    if (!gitScmCommandBuilder.isPresent()) {
      return newArrayList();
    }

    if (refId.startsWith(TAGS.getPath())) {
      return getTag(type, toHash, gitScmCommandBuilder);
    } else {
      return getCommits(fromHash, toHash, gitScmCommandBuilder, settings);
    }
  }

  private List<SbccChangeSet> getCommits(
      String fromHash,
      String toHash,
      Optional<GitScmCommandBuilder> gitScmCommandBuilder,
      SbccSettings settings) {
    final GitRevListBuilder revListBuilder =
        gitScmCommandBuilder
            .get() //
            .revList() //
            .format(FORMAT);
    if (fromHash.equals(FIRST_COMMIT)) {
      revListBuilder //
          .revs(toHash, "--not", "--all");
    } else {
      revListBuilder //
          .revs("^" + fromHash, toHash);
    }

    final List<SbccChangeSet> found =
        revListBuilder //
            .build(new RevListOutputHandler(settings)) //
            .call();

    if (found != null) {
      for (final SbccChangeSet sbccChangeSet : found) {
        logger.log(INFO, "Checking " + sbccChangeSet.getId());
      }
      return found;
    }
    return newArrayList();
  }

  private List<SbccChangeSet> getTag(
      RefChangeType type, String toHash, Optional<GitScmCommandBuilder> gitScmCommandBuilder) {
    if (type == DELETE) {
      return new ArrayList<>();
    }

    final SbccChangeSet sbccChangeSet =
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
