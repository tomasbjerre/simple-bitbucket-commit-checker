package se.bjurr.sscc;

import java.util.Map;

import com.atlassian.stash.user.DetailedUser;

public interface SsccUserAdminService {
 Map<String, DetailedUser> getStashUsers();
}
