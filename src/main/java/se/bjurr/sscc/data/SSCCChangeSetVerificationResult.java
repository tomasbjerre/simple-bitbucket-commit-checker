package se.bjurr.sscc.data;

import static com.google.common.base.Optional.absent;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newTreeMap;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.settings.SSCCGroup.Accept.ACCEPT;

import java.util.List;
import java.util.Map;

import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCMatch;

import com.google.common.base.Optional;

public class SSCCChangeSetVerificationResult {
 private Map<SSCCGroup, SSCCMatch> groupsResult = newTreeMap();
 private boolean nameAuthorResult;
 private boolean nameCommitterResult;
 private boolean emailAuthorResult;
 private boolean emailCommitterResult;
 private Map<String, Long> exceeding = newTreeMap();
 private Optional<String> rejectedContent = absent();
 private List<String> failingJqls = newArrayList();

 public Boolean getEmailAuthorResult() {
  return emailAuthorResult;
 }

 public Map<SSCCGroup, SSCCMatch> getGroupsResult() {
  return groupsResult;
 }

 public boolean getNameAuthorResult() {
  return nameAuthorResult;
 }

 public boolean getNameCommitterResult() {
  return nameCommitterResult;
 }

 public boolean hasErrors() {
  for (final SSCCGroup g : groupsResult.keySet()) {
   if (g.getAccept().equals(ACCEPT)) {
    return TRUE;
   }
  }
  return getRejectedContent().isPresent() || getExceeding().size() > 0 || !emailAuthorResult || !emailCommitterResult
    || !nameAuthorResult || !nameCommitterResult || !failingJqls.isEmpty();
 }

 public boolean hasReportables() {
  return !groupsResult.isEmpty() || hasErrors();
 }

 public void setEmailAuthorResult(Boolean emailResult) {
  this.emailAuthorResult = emailResult;
 }

 public void setEmailCommitterResult(boolean emailResult) {
  this.emailCommitterResult = emailResult;
 }

 public void setGroupsResult(Map<SSCCGroup, SSCCMatch> groupsResult) {
  this.groupsResult = groupsResult;
 }

 public void setNameAuthorResult(boolean nameResult) {
  this.nameAuthorResult = nameResult;
 }

 public void setNameCommitterResult(boolean nameResult) {
  this.nameCommitterResult = nameResult;
 }

 public void addContentSizeValidationResult(Map<String, Long> exceeding) {
  this.exceeding = exceeding;
 }

 public void addContentDiffValidationResult(Optional<String> rejectedContent) {
  this.rejectedContent = rejectedContent;
 }

 public Map<String, Long> getExceeding() {
  return exceeding;
 }

 public Optional<String> getRejectedContent() {
  return rejectedContent;
 }

 public boolean isEmailAuthorResult() {
  return emailAuthorResult;
 }

 public boolean getEmailCommitterResult() {
  return emailCommitterResult;
 }

 public void setFailingJql(List<String> queries) {
  this.failingJqls = queries;
 }

 public List<String> getFailingJqls() {
  return failingJqls;
 }
}
