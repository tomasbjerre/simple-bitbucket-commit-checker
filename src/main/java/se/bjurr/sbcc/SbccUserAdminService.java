package se.bjurr.sbcc;

import java.util.Map;

import com.atlassian.bitbucket.user.DetailedUser;

public interface SbccUserAdminService {
 Map<String, DetailedUser> getBitbucketUsers();
}
