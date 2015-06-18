package se.bjurr.sscc;

import java.io.IOException;
import java.util.List;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;

public interface ChangeSetsService {
 List<SSCCChangeSet> getNewChangeSets(SSCCSettings settings, Repository repository, String refId, RefChangeType type,
   String fromHash, String toHash) throws IOException;
}
