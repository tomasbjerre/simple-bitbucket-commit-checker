package se.bjurr.sbcc;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.concurrent.ExecutionException;

import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.UserService;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class SbccUserAdminServiceImpl implements SbccUserAdminService {

 private final LoadingCache<String, Boolean> displayNameCache = newBuilder()//
   .maximumSize(10000)//
   .expireAfterWrite(10, MINUTES)//
   .build(new CacheLoader<String, Boolean>() {
    @Override
    public Boolean load(String key) {
     return doDisplayNameExist(key);
    }
   });

 private final LoadingCache<String, Boolean> emailCache = newBuilder()//
   .maximumSize(10000)//
   .expireAfterWrite(10, MINUTES)//
   .build(new CacheLoader<String, Boolean>() {
    @Override
    public Boolean load(String key) {
     return doEmailExist(key);
    }
   });

 private final UserService userService;

 public SbccUserAdminServiceImpl(UserService userService) {
  this.userService = userService;
 }

 @Override
 public boolean displayNameExists(String name) {
  try {
   return this.displayNameCache.get(name);
  } catch (ExecutionException e) {
   throw propagate(e);
  }
 }

 @Override
 public boolean emailExists(String email) {
  try {
   return this.emailCache.get(email);
  } catch (ExecutionException e) {
   throw propagate(e);
  }
 }

 private boolean doDisplayNameExist(String name) {
  Page<ApplicationUser> found = getMatching(name);
  for (ApplicationUser f : found.getValues()) {
   if (f.getDisplayName().equalsIgnoreCase(name)) {
    return TRUE;
   }
  }
  return FALSE;
 }

 private boolean doEmailExist(String email) {
  Page<ApplicationUser> found = getMatching(email);
  for (ApplicationUser f : found.getValues()) {
   if (f.getEmailAddress().equalsIgnoreCase(email)) {
    return TRUE;
   }
  }
  return FALSE;
 }

 private Page<ApplicationUser> getMatching(String nameOrEmail) {
  int start = 0;
  int limit = 1000;
  PageRequest pagedRequest = new PageRequestImpl(start, limit);
  return this.userService.findUsersByName(nameOrEmail, pagedRequest);
 }
}
