package se.bjurr.sscc.data;

import java.util.Map;

public class SSCCChangeSet implements Comparable<SSCCChangeSet> {
 private final SSCCPerson committer;

 private final String id;

 private final String message;

 private final int parentCount;

 private final Map<String, Long> sizePerFile;

 private final String diff;

 public SSCCChangeSet(String id, SSCCPerson committer, String message, int parentCount, Map<String, Long> sizePerFile,
   String diff) {
  this.id = id;
  this.committer = committer;
  this.message = message.trim();
  this.parentCount = parentCount;
  this.sizePerFile = sizePerFile;
  this.diff = diff;
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

 public Map<String, Long> getSizePerFile() {
  return sizePerFile;
 }

 public String getDiff() {
  return diff;
 }
}
