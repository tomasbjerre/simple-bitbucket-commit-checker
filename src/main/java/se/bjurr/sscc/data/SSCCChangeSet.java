package se.bjurr.sscc.data;

public class SSCCChangeSet implements Comparable<SSCCChangeSet> {
 private final SSCCPerson committer;

 private final String id;

 private final String message;

 private final int parentCount;

 public SSCCChangeSet(String id, SSCCPerson committer, String message, int parentCount) {
  this.id = id;
  this.committer = committer;
  this.message = message.trim();
  this.parentCount = parentCount;
 }

 @Override
 public int compareTo(SSCCChangeSet o) {
  return id.compareTo(o.id);
 }

 public SSCCPerson getCommitter() {
  return committer;
 }

 /**
  * The commit hash
  */
 public String getId() {
  return id;
 }

 public String getMessage() {
  return message;
 }

 public int getParentCount() {
  return parentCount;
 }
}
