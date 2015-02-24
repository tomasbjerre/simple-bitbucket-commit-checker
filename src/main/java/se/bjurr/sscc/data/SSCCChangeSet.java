package se.bjurr.sscc.data;

public class SSCCChangeSet {
	private final SSCCPerson committer;

	private final String id;

	private final String message;

	private final int parentCount;

	public SSCCChangeSet(String id, SSCCPerson committer, String message, int parentCount) {
		this.id = id;
		this.committer = committer;
		this.message = removeTrailingNewLine(message);
		this.parentCount = parentCount;
	}

	public SSCCPerson getCommitter() {
		return committer;
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public int getParentCount() {
		return parentCount;
	}

	private String removeTrailingNewLine(String str) {
		if (str.endsWith("\n")) {
			str = str.substring(0, str.length() - 1);
		}

		return str;
	}
}
