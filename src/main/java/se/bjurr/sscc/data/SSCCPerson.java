package se.bjurr.sscc.data;


public class SSCCPerson {

	private final String emailAddress;

	private final String name;

	public SSCCPerson(String name, String emailAddress) {
		this.name = name;
		this.emailAddress = emailAddress;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getName() {
		return name;
	}
}
