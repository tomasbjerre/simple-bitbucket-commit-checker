package se.bjurr.sscc;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.io.UnsupportedEncodingException;
import java.util.List;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.annotations.VisibleForTesting;

public class JqlValidator {
 private final SSCCSettings settings;
 private final SSCCRenderer renderer;
 private final ApplicationLinkService applicationLinkService;
 private static JiraClient jiraClient = new JiraClient();

 public JqlValidator(ApplicationLinkService applicationLinkService, SSCCSettings settings, SSCCRenderer renderer) {
  this.settings = settings;
  this.renderer = renderer;
  this.applicationLinkService = applicationLinkService;
 }

 public List<String> validateJql(SSCCChangeSet ssccChangeSet) throws CredentialsRequiredException,
   UnsupportedEncodingException, ResponseException {
  List<String> failingJqls = newArrayList();
  if (!settings.shouldCheckJql()) {
   return failingJqls;
  }
  if (settings.getCommitRegexp().isPresent()) {
   for (String renderedJqlQUery : renderer.renderAll(SSCCRenderer.SSCCVariable.REGEXP,
     settings.getCommitRegexp().get(), ssccChangeSet, settings.getJqlCheckQuery())) {
    if (addJqlQuery(failingJqls, renderedJqlQUery)) {
     return newArrayList();
    }
   }
  } else {
   if (addJqlQuery(failingJqls, renderer.render(settings.getJqlCheckQuery()))) {
    return newArrayList();
   }
  }
  return failingJqls;
 }

 private Boolean addJqlQuery(List<String> failingJqls, String renderedJqlQUery) throws CredentialsRequiredException,
   UnsupportedEncodingException, ResponseException {
  if (jiraClient.getNumberOfJqlResults(applicationLinkService, renderedJqlQUery) > 0) {
   return TRUE;
  }
  failingJqls.add(renderedJqlQUery);
  return FALSE;
 }

 @VisibleForTesting
 public static void setJiraClient(JiraClient jiraClient) {
  JqlValidator.jiraClient = jiraClient;
 }
}
