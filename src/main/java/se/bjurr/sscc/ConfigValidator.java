package se.bjurr.sscc;

import javax.annotation.Nonnull;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;

public class ConfigValidator implements RepositorySettingsValidator {
	public ConfigValidator() {

	}

	@Override
	public void validate(@Nonnull Settings settings, @Nonnull SettingsValidationErrors errors,
			@Nonnull Repository repository) {
	}
}
