package se.bjurr.sbcc.data;

public class SbccChangeSet implements Comparable<SbccChangeSet> {
  private final SbccPerson author;

  private final SbccPerson committer;

  private final String id;

  private final String message;

  private final boolean tag;

  public SbccChangeSet(
      final String id,
      final SbccPerson committer,
      final SbccPerson author,
      final String message,
      final boolean tag) {
    this.id = id;
    this.author = author;
    this.committer = committer;
    this.message = message.trim();
    this.tag = tag;
  }

  @Override
  public int compareTo(final SbccChangeSet o) {
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

  public boolean isTag() {
    return tag;
  }

  public String getMessage() {
    return this.message;
  }
}
