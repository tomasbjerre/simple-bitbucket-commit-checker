package se.bjurr.sscc.data;

public class SSCCChangeSetBuilder {
	public static SSCCChangeSetBuilder changeSetBuilder() {
		return new SSCCChangeSetBuilder();
	}

	private SSCCPerson committer;

	private String id;
	private String message;
	private int parentCount;

	private SSCCChangeSetBuilder() {
	}

	public SSCCChangeSet build() {
		return new SSCCChangeSet(id, committer, message, parentCount);
	}

	public SSCCChangeSetBuilder withCommitter(SSCCPerson committer) {
		this.committer = committer;
		return this;
	}

	public SSCCChangeSetBuilder withId(String id) {
		this.id = id;
		return this;
	}

	public SSCCChangeSetBuilder withMessage(String message) {
		this.message = message;
		return this;
	}

	public SSCCChangeSetBuilder withParentCount(int parentCount) {
		this.parentCount = parentCount;
		return this;
	}
}
