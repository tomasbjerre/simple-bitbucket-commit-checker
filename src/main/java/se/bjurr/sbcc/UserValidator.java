package se.bjurr.sbcc;

import static com.atlassian.bitbucket.user.UserType.SERVICE;
import static java.util.regex.Pattern.matches;
import se.bjurr.sbcc.settings.SbccSettings;

import com.atlassian.bitbucket.user.ApplicationUser;

public class UserValidator {

  private final ApplicationUser currentUser;
  private final SbccSettings settings;

  public UserValidator(SbccSettings settings, ApplicationUser currentUser) {
    this.currentUser = currentUser;
    this.settings = settings;
  }

  public boolean shouldIgnoreChecksForUser() {
    return shouldIgnoreServiceUser() || shouldIgnoreByUserNamePattern();
  }

  private boolean shouldIgnoreByUserNamePattern() {
    return settings.getIgnoreUsersPattern().isPresent()
        && (currentUser == null
            || matches(settings.getIgnoreUsersPattern().get(), currentUser.getName()));
  }

  private boolean shouldIgnoreServiceUser() {
    return settings.allowServiceUsers()
        && currentUser != null
        && currentUser.getType().equals(SERVICE);
  }
}
