package se.bjurr.sbcc.commits;

import java.io.IOException;
import java.util.List;

import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.settings.SbccSettings;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;

public interface ChangeSetsService {
  List<SbccChangeSet> getNewChangeSets(
      SbccSettings settings,
      Repository fromRepository,
      String refId,
      RefChangeType type,
      String fromHash,
      String toHash)
      throws IOException;

  List<SbccChangeSet> getNewChangeSets(SbccSettings settings, PullRequest pullRequest)
      throws IOException;
}
