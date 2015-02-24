package se.bjurr.sscc.util;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.when;

import java.util.List;

import se.bjurr.sscc.ChangeSetsService;
import se.bjurr.sscc.data.SSCCChangeSet;

import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;

public class RefChangeBuilder {
	public static RefChangeBuilder refChangeBuilder(Repository repository, ChangeSetsService changeSetService) {
		return new RefChangeBuilder(repository, changeSetService);
	}

	private final ChangeSetsService changeSetService;
	private String fromHash;
	private final List<SSCCChangeSet> newChangesets;

	private String refId;
	private final Repository repository;
	private String toHash;
	private RefChangeType type;

	private RefChangeBuilder(Repository repository, ChangeSetsService changeSetService) {
		newChangesets = newArrayList();
		this.repository = repository;
		this.changeSetService = changeSetService;
	}

	public RefChange build() {
		RefChange refChange = new RefChange() {

			@Override
			public String getFromHash() {
				return fromHash;
			}

			@Override
			public String getRefId() {
				return refId;
			}

			@Override
			public String getToHash() {
				return toHash;
			}

			@Override
			public RefChangeType getType() {
				return type;
			}
		};

		when(changeSetService.getNewChangeSets(repository, refChange)).thenReturn(newChangesets);

		return refChange;
	}

	public RefChangeBuilder withChangeSet(SSCCChangeSet changeSet) {
		newChangesets.add(changeSet);
		return this;
	}
}
