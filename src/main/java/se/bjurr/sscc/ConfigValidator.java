package se.bjurr.sscc;

import static se.bjurr.sscc.settings.SSCCSettings.sscSettings;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

import se.bjurr.sscc.settings.SSCCSettings;
import se.bjurr.sscc.settings.ValidationException;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;

public class ConfigValidator implements RepositorySettingsValidator {
 private static final Logger logger = Logger.getLogger(RepositorySettingsValidator.class.getName());

 public ConfigValidator() {

 }

 @Override
 public void validate(@Nonnull Settings settings, @Nonnull SettingsValidationErrors errors,
   @Nonnull Repository repository) {
  try {
   final SSCCSettings ssccSettings = sscSettings(settings);
   logger.fine("Validating:\n" + ssccSettings.toString());
  } catch (final ValidationException e) {
   errors.addFieldError(e.getField(), e.getError());
  }
 }
}
