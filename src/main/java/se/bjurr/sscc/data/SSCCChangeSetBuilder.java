package se.bjurr.sscc.data;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.Maps.newTreeMap;

import java.util.Map;

public class SSCCChangeSetBuilder {
 private SSCCPerson committer;
 private SSCCPerson author;
 private String id;
 private String message;
 private int parentCount;
 private final Map<String, Long> fileSizeBytes = newTreeMap();
 private String diff;

 public static SSCCChangeSetBuilder changeSetBuilder() {
  return new SSCCChangeSetBuilder();
 }

 private SSCCChangeSetBuilder() {
 }

 public SSCCChangeSet build() {
  return new SSCCChangeSet(id, firstNonNull(committer, new SSCCPerson("Tomas", "my@email.com")), firstNonNull(author,
    new SSCCPerson("Tomas", "my@email.com")), message, parentCount, fileSizeBytes, diff);
 }

 public SSCCChangeSetBuilder withCommitter(SSCCPerson committer) {
  this.committer = committer;
  return this;
 }

 public SSCCChangeSetBuilder withAuthor(SSCCPerson author) {
  this.author = author;
  return this;
 }

 public SSCCChangeSetBuilder withId(String id) {
  this.id = id;
  return this;
 }

 public SSCCChangeSetBuilder withMessage(String message) {
  this.message = message;
  return this;
 }

 public SSCCChangeSetBuilder withParentCount(int parentCount) {
  this.parentCount = parentCount;
  return this;
 }

 public SSCCChangeSetBuilder withDiff(String diff) {
  this.diff = diff;
  return this;
 }

 public SSCCChangeSetBuilder withSize(String path, Long fileSizeBytes) {
  this.fileSizeBytes.put(path, fileSizeBytes);
  return this;
 }
}
