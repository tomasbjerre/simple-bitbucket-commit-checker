package se.bjurr.sbcc;

import static se.bjurr.sbcc.settings.SbccSettings.sscSettings;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.scope.Scope;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;
import com.atlassian.bitbucket.setting.SettingsValidator;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import se.bjurr.sbcc.settings.SbccSettings;
import se.bjurr.sbcc.settings.ValidationException;

public class ConfigValidator implements SettingsValidator {
  private static final Logger logger = Logger.getLogger(ConfigValidator.class.getName());
  private final AuthenticationContext authenticationContext;

  public ConfigValidator(AuthenticationContext authenticationContext) {
    this.authenticationContext = authenticationContext;
  }

  @Override
  public void validate(
      @Nonnull final Settings settings,
      @Nonnull final SettingsValidationErrors errors,
      @Nonnull final Scope scope) {
    try {
      SbccRenderer sbccRenderer = new SbccRenderer(this.authenticationContext);
      final SbccSettings sbccSettings = sscSettings(new RenderingSettings(settings, sbccRenderer));
      logger.fine("Validating:\n" + sbccSettings.toString());
    } catch (final ValidationException e) {
      errors.addFieldError(e.getField(), e.getError());
    }
  }
}
