package se.bjurr.sscc;

import static se.bjurr.sscc.settings.SSCCSettings.sscSettings;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.sscc.settings.SSCCSettings;
import se.bjurr.sscc.settings.ValidationException;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;

public class ConfigValidator implements RepositorySettingsValidator {
 private static final Logger logger = LoggerFactory.getLogger(RepositorySettingsValidator.class);

 public ConfigValidator() {

 }

 @Override
 public void validate(@Nonnull Settings settings, @Nonnull SettingsValidationErrors errors,
   @Nonnull Repository repository) {
  try {
   final SSCCSettings ssccSettings = sscSettings(settings);
   logger.debug("Validating:\n" + ssccSettings.toString());
  } catch (final ValidationException e) {
   errors.addFieldError(e.getField(), e.getError());
  }
 }
}
