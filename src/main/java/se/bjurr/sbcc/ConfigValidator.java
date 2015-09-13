package se.bjurr.sbcc;

import static se.bjurr.sbcc.settings.SbccSettings.sscSettings;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

import se.bjurr.sbcc.settings.SbccSettings;
import se.bjurr.sbcc.settings.ValidationException;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.RepositorySettingsValidator;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;

public class ConfigValidator implements RepositorySettingsValidator {
 private static final Logger logger = Logger.getLogger(RepositorySettingsValidator.class.getName());

 public ConfigValidator() {

 }

 @Override
 public void validate(@Nonnull Settings settings, @Nonnull SettingsValidationErrors errors,
   @Nonnull Repository repository) {
  try {
   final SbccSettings sbccSettings = sscSettings(settings);
   logger.fine("Validating:\n" + sbccSettings.toString());
  } catch (final ValidationException e) {
   errors.addFieldError(e.getField(), e.getError());
  }
 }
}
