package se.bjurr.sbcc;

import com.atlassian.bitbucket.auth.AuthenticationContext;

public class SbccCommon {

 public static String getBitbucketEmail(AuthenticationContext bitbucketAuthenticationContext) {
  if (bitbucketAuthenticationContext == null || bitbucketAuthenticationContext.getCurrentUser() == null
    || bitbucketAuthenticationContext.getCurrentUser().getEmailAddress() == null) {
   return "Unset";
  }
  return bitbucketAuthenticationContext.getCurrentUser().getEmailAddress();
 }

 public static String getBitbucketName(AuthenticationContext bitbucketAuthenticationContext) {
  if (bitbucketAuthenticationContext == null || bitbucketAuthenticationContext.getCurrentUser() == null
    || bitbucketAuthenticationContext.getCurrentUser().getDisplayName() == null) {
   return "Unset";
  }
  return bitbucketAuthenticationContext.getCurrentUser().getDisplayName();
 }

 public static String getBitbucketUser(AuthenticationContext bitbucketAuthenticationContext) {
  if (bitbucketAuthenticationContext == null || bitbucketAuthenticationContext.getCurrentUser() == null
    || bitbucketAuthenticationContext.getCurrentUser().getName() == null) {
   return "Unset";
  }
  return bitbucketAuthenticationContext.getCurrentUser().getName();
 }

 public static String getBitbucketUserSlug(AuthenticationContext bitbucketAuthenticationContext) {
  if (bitbucketAuthenticationContext == null || bitbucketAuthenticationContext.getCurrentUser() == null
    || bitbucketAuthenticationContext.getCurrentUser().getSlug() == null) {
   return "Unset";
  }
  return bitbucketAuthenticationContext.getCurrentUser().getSlug();
 }

}
