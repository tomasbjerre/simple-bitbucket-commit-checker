package se.bjurr.sbcc.data;

public class SbccChangeSet implements Comparable<SbccChangeSet> {
  private final SbccPerson author;

  private final SbccPerson committer;

  private final String id;

  private final String message;

  public SbccChangeSet(String id, SbccPerson committer, SbccPerson author, String message) {
    this.id = id;
    this.author = author;
    this.committer = committer;
    this.message = message.trim();
  }

  @Override
  public int compareTo(SbccChangeSet o) {
    return this.id.compareTo(o.id);
  }

  public SbccPerson getAuthor() {
    return this.author;
  }

  public SbccPerson getCommitter() {
    return this.committer;
  }

  /** The commit hash */
  public String getId() {
    return this.id;
  }

  public String getMessage() {
    return this.message;
  }
}
