package se.bjurr.sbcc.data;

import java.util.Map;

public class SbccChangeSet implements Comparable<SbccChangeSet> {
 private final SbccPerson committer;

 private final String id;

 private final String message;

 private final int parentCount;

 private final Map<String, Long> sizePerFile;

 private final String diff;

 private final SbccPerson author;

 public SbccChangeSet(String id, SbccPerson committer, SbccPerson author, String message, int parentCount,
   Map<String, Long> sizePerFile, String diff) {
  this.id = id;
  this.author = author;
  this.committer = committer;
  this.message = message.trim();
  this.parentCount = parentCount;
  this.sizePerFile = sizePerFile;
  this.diff = diff;
 }

 @Override
 public int compareTo(SbccChangeSet o) {
  return id.compareTo(o.id);
 }

 public SbccPerson getCommitter() {
  return committer;
 }

 public SbccPerson getAuthor() {
  return author;
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
