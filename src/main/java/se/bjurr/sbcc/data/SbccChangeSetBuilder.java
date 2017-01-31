package se.bjurr.sbcc.data;

import static com.google.common.base.MoreObjects.firstNonNull;

public class SbccChangeSetBuilder {
  public static final SbccPerson DEFAULT_AUTHOR = new SbccPerson("Tomas", "my@email.com");
  public static final SbccPerson DEFAULT_COMMITTER = new SbccPerson("Tomas", "my@email.com");

  public static SbccChangeSetBuilder changeSetBuilder() {
    return new SbccChangeSetBuilder();
  }

  private SbccPerson author;
  private SbccPerson committer;
  private String id;
  private String message;

  private SbccChangeSetBuilder() {}

  public SbccChangeSet build() {
    return new SbccChangeSet(
        this.id,
        firstNonNull(this.committer, DEFAULT_COMMITTER),
        firstNonNull(this.author, DEFAULT_AUTHOR),
        this.message);
  }

  public SbccChangeSetBuilder withAuthor(SbccPerson author) {
    this.author = author;
    return this;
  }

  public SbccChangeSetBuilder withCommitter(SbccPerson committer) {
    this.committer = committer;
    return this;
  }

  public SbccChangeSetBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public SbccChangeSetBuilder withMessage(String message) {
    this.message = message;
    return this;
  }
}
