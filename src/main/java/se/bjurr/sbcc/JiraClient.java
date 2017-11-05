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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraClient {
  private Logger logger = LoggerFactory.getLogger(JiraClient.class);

  public int getNumberOfJqlResults(
      ApplicationLinkService applicationLinkService, String jqlCheckQuery)
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
    String restPath = "/rest/api/2/search?jql=" + encode(jqlCheckQuery, UTF_8.name());
    String json =
        applicationLinkService
            .getPrimaryApplicationLink(JiraApplicationType.class)
            .createAuthenticatedRequestFactory()
            .createRequest(GET, restPath)
            .execute();
    logger.debug(restPath + "\n\n <<< " + json);
    return json;
  }
}
