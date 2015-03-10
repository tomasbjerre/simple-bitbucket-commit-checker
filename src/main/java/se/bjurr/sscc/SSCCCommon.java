package se.bjurr.sscc;

import com.atlassian.stash.user.StashAuthenticationContext;

public class SSCCCommon {

 public static String getStashEmail(StashAuthenticationContext stashAuthenticationContext) {
  if (stashAuthenticationContext == null || stashAuthenticationContext.getCurrentUser() == null
    || stashAuthenticationContext.getCurrentUser().getEmailAddress() == null) {
   return "Unset";
  }
  return stashAuthenticationContext.getCurrentUser().getEmailAddress();
 }

 public static String getStashName(StashAuthenticationContext stashAuthenticationContext) {
  if (stashAuthenticationContext == null || stashAuthenticationContext.getCurrentUser() == null
    || stashAuthenticationContext.getCurrentUser().getDisplayName() == null) {
   return "Unset";
  }
  return stashAuthenticationContext.getCurrentUser().getDisplayName();
 }

}
