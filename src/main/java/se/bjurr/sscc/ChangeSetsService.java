package se.bjurr.sscc;

import java.util.List;

import se.bjurr.sscc.data.SSCCChangeSet;

import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;

public interface ChangeSetsService {
	public List<SSCCChangeSet> getNewChangeSets(Repository repository, RefChange refChange);
}
