package se.bjurr.sbcc.data;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.Maps.newTreeMap;

import java.util.Map;

public class SbccChangeSetBuilder {
 public static final SbccPerson DEFAULT_COMMITTER = new SbccPerson("Tomas", "my@email.com");
 public static final SbccPerson DEFAULT_AUTHOR = new SbccPerson("Tomas", "my@email.com");
 private SbccPerson committer;
 private SbccPerson author;
 private String id;
 private String message;
 private int parentCount;
 private final Map<String, Long> fileSizeBytes = newTreeMap();
 private String diff;

 public static SbccChangeSetBuilder changeSetBuilder() {
  return new SbccChangeSetBuilder();
 }

 private SbccChangeSetBuilder() {
 }

 public SbccChangeSet build() {
  return new SbccChangeSet(id, firstNonNull(committer, DEFAULT_COMMITTER), firstNonNull(author, DEFAULT_AUTHOR),
    message, parentCount, fileSizeBytes, diff);
 }

 public SbccChangeSetBuilder withCommitter(SbccPerson committer) {
  this.committer = committer;
  return this;
 }

 public SbccChangeSetBuilder withAuthor(SbccPerson author) {
  this.author = author;
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

 public SbccChangeSetBuilder withParentCount(int parentCount) {
  this.parentCount = parentCount;
  return this;
 }

 public SbccChangeSetBuilder withDiff(String diff) {
  this.diff = diff;
  return this;
 }

 public SbccChangeSetBuilder withSize(String path, Long fileSizeBytes) {
  this.fileSizeBytes.put(path, fileSizeBytes);
  return this;
 }
}
