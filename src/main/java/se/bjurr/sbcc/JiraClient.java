package se.bjurr.sbcc;

import static com.atlassian.sal.api.net.Request.MethodType.GET;
import static com.google.common.base.Charsets.UTF_8;
import static java.net.URLEncoder.encode;

import java.io.UnsupportedEncodingException;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JiraClient {
 public int getNumberOfJqlResults(ApplicationLinkService applicationLinkService, String jqlCheckQuery)
   throws CredentialsRequiredException, UnsupportedEncodingException, ResponseException {
  try {
   String json = invokeJira(applicationLinkService, jqlCheckQuery);
   JsonObject response = new JsonParser().parse(json).getAsJsonObject();
   return response.get("issues").getAsJsonArray().size();
  } catch (Exception e) {
   return 0;
  }
 }

 @VisibleForTesting
 protected String invokeJira(ApplicationLinkService applicationLinkService, String jqlCheckQuery)
   throws UnsupportedEncodingException, ResponseException, CredentialsRequiredException {
  return applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class)
    .createAuthenticatedRequestFactory()
    .createRequest(GET, "/rest/api/2/search?jql=" + encode(jqlCheckQuery, UTF_8.name())).execute();
 }
}
