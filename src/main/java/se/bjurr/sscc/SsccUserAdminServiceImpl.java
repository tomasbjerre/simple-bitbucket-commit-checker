package se.bjurr.sscc;

import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Map;

import com.atlassian.stash.user.DetailedUser;
import com.atlassian.stash.user.UserAdminService;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageRequestImpl;
import com.google.common.base.Supplier;

public class SsccUserAdminServiceImpl implements SsccUserAdminService {

 private final UserAdminService userAdminService;

 public SsccUserAdminServiceImpl(UserAdminService userAdminService) {
  this.userAdminService = userAdminService;
 }

 private final Supplier<Map<String, DetailedUser>> stashUsers = memoizeWithExpiration(
   new Supplier<Map<String, DetailedUser>>() {
    @Override
    public java.util.Map<String, DetailedUser> get() {
     final Map<String, DetailedUser> map = newHashMap();
     for (DetailedUser detailedUser : userAdminService.findUsers(new PageRequest() {

      public String getFilter() {
       // This method is not available in Sash 2.12.0, but in 3
       return "";
      }

      @Override
      public int getStart() {
       getFilter(); // Ensure save-actions does not remove the method
       return 0;
      }

      @Override
      public int getLimit() {
       return 1048575;
      }

      @Override
      public PageRequest buildRestrictedPageRequest(int arg0) {
       return new PageRequestImpl(0, 1048575);
      }
     }).getValues()) {
      map.put(detailedUser.getDisplayName(), detailedUser);
      map.put(detailedUser.getEmailAddress(), detailedUser);
      map.put(detailedUser.getName(), detailedUser);
     }
     return map;
    };
   }, 5, MINUTES);

 @Override
 public Map<String, DetailedUser> getStashUsers() {
  return stashUsers.get();
 }
}
