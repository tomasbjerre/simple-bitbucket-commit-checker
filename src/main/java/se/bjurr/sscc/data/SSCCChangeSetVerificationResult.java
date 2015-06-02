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
 private boolean emailAuthorResult;
 private Map<SSCCGroup, SSCCMatch> groupsResult = newTreeMap();
 private boolean nameResult;
 private Map<String, Long> exceeding = newTreeMap();
 private Optional<String> rejectedContent = absent();
 private boolean emailCommitterResult;

 public Boolean getEmailAuthorResult() {
  return emailAuthorResult;
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
  return getRejectedContent().isPresent() || getExceeding().size() > 0 || !emailAuthorResult || !emailCommitterResult
    || !nameResult;
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

 public boolean isEmailAuthorResult() {
  return emailAuthorResult;
 }

 public boolean getEmailCommitterResult() {
  return emailCommitterResult;
 }
}
