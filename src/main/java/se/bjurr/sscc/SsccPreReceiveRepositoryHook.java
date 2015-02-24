package se.bjurr.sscc;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Suppliers.memoize;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Resources.getResource;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import se.bjurr.sscc.data.SSCCChangeSet;

import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.PreReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;

public class SsccPreReceiveRepositoryHook implements PreReceiveRepositoryHook {
	private static String RESPONSE_REJECT_TXT = "response_reject.txt";
	private static String RESPONSE_SUCCESS_TXT = "response_success.txt";
	private String responseRejectTxt = RESPONSE_REJECT_TXT;
	private String responseSuccessTxt = RESPONSE_SUCCESS_TXT;

	@VisibleForTesting
	public void setRESPONSE_REJECT_TXT(String filename) {
		responseRejectTxt = filename;
	}

	@VisibleForTesting
	public void setRESPONSE_SUCCESS_TXT(String filename) {
		responseSuccessTxt = filename;
	}

	private ChangeSetsService changesetsService;

	private Supplier<String> responseReject;
	private Supplier<String> responseSuccess;

	public SsccPreReceiveRepositoryHook(ChangeSetsService changesetsService) {
		this.changesetsService = changesetsService;
		responseReject = memoize(new Supplier<String>() {
			@Override
			public String get() {
				try {
					return com.google.common.io.Resources.toString(getResource(responseRejectTxt), UTF_8);
				} catch (final IOException e) {
					propagate(e);
					return "";
				}
			}
		});
		responseSuccess = memoize(new Supplier<String>() {
			@Override
			public String get() {
				try {
					return com.google.common.io.Resources.toString(getResource(responseSuccessTxt), UTF_8);
				} catch (final IOException e) {
					propagate(e);
					return "";
				}
			}
		});
	}

	private List<String> checkRefChange(Repository repository, Settings settings, RefChange refChange) {
		final List<String> errors = newArrayList();
		if (refChange.getType() == RefChangeType.DELETE) {
			return errors;
		}
		for (final SSCCChangeSet ssccChangeSet : changesetsService.getNewChangeSets(repository, refChange)) {
			if (ssccChangeSet.getMessage().indexOf("SB-") == -1) {
				errors.add("REJECTING: " + ssccChangeSet.getId() + " " + ssccChangeSet.getMessage());
			}
		}
		return errors;
	}

	@Override
	public boolean onReceive(RepositoryHookContext repositoryHookContext, Collection<RefChange> refChanges,
			HookResponse hookResponse) {
		final List<String> errors = newArrayList();

		for (final RefChange refChange : refChanges) {
			errors.addAll(checkRefChange(repositoryHookContext.getRepository(), repositoryHookContext.getSettings(),
					refChange));
		}

		if (errors.isEmpty()) {
			hookResponse.out().println(responseSuccess.get());
			hookResponse.out().println();
			return true;
		} else {
			hookResponse.err().println(responseReject.get());
			for (final String error : errors) {
				hookResponse.err().println(error);
			}
			hookResponse.err().println();
			return false;
		}
	}

	@VisibleForTesting
	public void setChangesetsService(ChangeSetsService changesetsService) {
		this.changesetsService = changesetsService;
	}
}
