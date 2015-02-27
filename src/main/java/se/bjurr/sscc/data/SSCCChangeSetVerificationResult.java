package se.bjurr.sscc.data;

import static com.google.common.collect.Maps.newTreeMap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static se.bjurr.sscc.settings.SSCCGroup.Accept.ACCEPT;

import java.util.Map;

import se.bjurr.sscc.settings.SSCCGroup;
import se.bjurr.sscc.settings.SSCCMatch;

public class SSCCChangeSetVerificationResult {
 private boolean emailResult;
 private Map<SSCCGroup, SSCCMatch> groupsResult = newTreeMap();
 private boolean nameResult;

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
  return FALSE;
 }

 public boolean hasReportables() {
  return !groupsResult.isEmpty() || !emailResult || !nameResult;
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
}
