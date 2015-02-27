package se.bjurr.sscc;

import java.io.IOException;
import java.util.List;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;

public interface ChangeSetsService {
 public List<SSCCChangeSet> getNewChangeSets(SSCCSettings settings, Repository repository, RefChange refChange)
   throws IOException;
}
