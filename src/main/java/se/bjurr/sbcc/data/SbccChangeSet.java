package se.bjurr.sbcc.data;

import java.util.TreeMap;

public class SbccChangeSet implements Comparable<SbccChangeSet> {
 private final SbccPerson author;

 private final SbccPerson committer;

 private final String diff;

 private final String id;

 private final String message;

 private final int parentCount;

 private final TreeMap<String, Long> sizeAboveLimitPerFile;

 public SbccChangeSet(String id, SbccPerson committer, SbccPerson author, String message, int parentCount,
   TreeMap<String, Long> sizeAboveLimitPerFile, String diff) {
  this.id = id;
  this.author = author;
  this.committer = committer;
  this.message = message.trim();
  this.parentCount = parentCount;
  this.sizeAboveLimitPerFile = sizeAboveLimitPerFile;
  this.diff = diff;
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

 public String getDiff() {
  return this.diff;
 }

 /**
  * The commit hash
  */
 public String getId() {
  return this.id;
 }

 public String getMessage() {
  return this.message;
 }

 public int getParentCount() {
  return this.parentCount;
 }

 public TreeMap<String, Long> getSizeAboveLimitPerFile() {
  return this.sizeAboveLimitPerFile;
 }
}
