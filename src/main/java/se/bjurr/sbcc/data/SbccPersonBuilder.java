package se.bjurr.sbcc.data;

public class SbccPersonBuilder {
  public static SbccPersonBuilder sbccPersonBuilder() {
    return new SbccPersonBuilder();
  }

  private String emailAddress;
  private String name;

  private SbccPersonBuilder() {}

  public SbccPerson build() {
    return new SbccPerson(name, emailAddress);
  }

  public SbccPersonBuilder withEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
    return this;
  }

  public SbccPersonBuilder withName(String name) {
    this.name = name;
    return this;
  }
}
