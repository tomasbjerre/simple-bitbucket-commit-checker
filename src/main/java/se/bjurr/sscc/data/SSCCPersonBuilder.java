package se.bjurr.sscc.data;

public class SSCCPersonBuilder {
	public static SSCCPersonBuilder ssccPersonBuilder() {
		return new SSCCPersonBuilder();
	}

	private String emailAddress;
	private String name;

	private SSCCPersonBuilder() {
	}

	public SSCCPerson build() {
		return new SSCCPerson(name, emailAddress);
	}

	public SSCCPersonBuilder withEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
		return this;
	}

	public SSCCPersonBuilder withName(String name) {
		this.name = name;
		return this;
	}
}
