package se.bjurr.sscc.data;

import static com.google.common.base.Optional.absent;
import static com.google.common.collect.Maps.newTreeMap;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.settings.SSCCGroup.Accept.ACCEPT;

import java.util.Map;

import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCMatch;

import com.google.common.base.Optional;

public class SSCCChangeSetVerificationResult {
 private boolean emailResult;
 private Map<SSCCGroup, SSCCMatch> groupsResult = newTreeMap();
 private boolean nameResult;
 private Map<String, Long> exceeding = newTreeMap();
 private Optional<String> rejectedContent = absent();

 public Boolean getEmailResult() {
  return emailResult;
 }

 public Map<SSCCGroup, SSCCMatch> getGroupsResult() {
  return groupsResult;
 }

 public boolean getNameResult() {
  return nameResult;
 }

 public boolean hasErrors() {
  for (final SSCCGroup g : groupsResult.keySet()) {
   if (g.getAccept().equals(ACCEPT)) {
    return TRUE;
   }
  }
  return getRejectedContent().isPresent() || getExceeding().size() > 0 || !emailResult || !nameResult;
 }

 public boolean hasReportables() {
  return !groupsResult.isEmpty() || hasErrors();
 }

 public void setEmailResult(Boolean emailResult) {
  this.emailResult = emailResult;
 }

 public void setGroupsResult(Map<SSCCGroup, SSCCMatch> groupsResult) {
  this.groupsResult = groupsResult;
 }

 public void setNameResult(boolean nameResult) {
  this.nameResult = nameResult;
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
}
