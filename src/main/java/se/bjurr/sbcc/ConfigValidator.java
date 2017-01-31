package se.bjurr.sbcc;

import static se.bjurr.sbcc.settings.SbccSettings.sscSettings;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.RepositorySettingsValidator;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;

import se.bjurr.sbcc.settings.SbccSettings;
import se.bjurr.sbcc.settings.ValidationException;

public class ConfigValidator implements RepositorySettingsValidator {
  private static final Logger logger =
      Logger.getLogger(RepositorySettingsValidator.class.getName());
  private final AuthenticationContext authenticationContext;

  public ConfigValidator(AuthenticationContext authenticationContext) {
    this.authenticationContext = authenticationContext;
  }

  @Override
  public void validate(
      @Nonnull Settings settings,
      @Nonnull SettingsValidationErrors errors,
      @Nonnull Repository repository) {
    try {
      SbccRenderer sbccRenderer = new SbccRenderer(this.authenticationContext);
      final SbccSettings sbccSettings = sscSettings(new RenderingSettings(settings, sbccRenderer));
      logger.fine("Validating:\n" + sbccSettings.toString());
    } catch (final ValidationException e) {
      errors.addFieldError(e.getField(), e.getError());
    }
  }
}
