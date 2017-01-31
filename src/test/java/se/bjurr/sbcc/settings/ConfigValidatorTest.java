package se.bjurr.sbcc.settings;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_BRANCHES;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_GROUP_ACCEPT;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_GROUP_MATCH;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_RULE_MESSAGE;
import static se.bjurr.sbcc.settings.SbccSettings.SETTING_RULE_REGEXP;
import static se.bjurr.sbcc.settings.SbccSettings.sscSettings;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;

import se.bjurr.sbcc.ConfigValidator;

public class ConfigValidatorTest {
  private AuthenticationContext authenticationContext;
  private ConfigValidator configValidator;
  private final SettingsValidationErrors errors =
      new SettingsValidationErrors() {
        @Override
        public void addFieldError(String field, String error) {
          ConfigValidatorTest.this.fieldErrors.put(field, error);
        }

        @Override
        public void addFormError(String error) {
          ConfigValidatorTest.this.formError.add(error);
        }
      };
  private final Map<String, String> fieldErrors = newHashMap();
  private final List<String> formError = newArrayList();
  private Repository repository;
  private Settings settings;

  @Before
  public void before() {
    this.settings = mock(Settings.class);
    when(this.settings.getBoolean(anyString())).thenReturn(null);
    when(this.settings.getString(anyString())).thenReturn(null);
    this.authenticationContext = mock(AuthenticationContext.class);
    this.configValidator = new ConfigValidator(this.authenticationContext);
  }

  @Test
  public void testThatBranchesCanBeEmpty() {
    when(this.settings.getString(SETTING_BRANCHES)).thenReturn("");
    this.configValidator.validate(this.settings, this.errors, this.repository);
    assertEquals("", on(",").join(this.fieldErrors.keySet()));
    assertEquals("", on(",").join(this.fieldErrors.values()));
  }

  @Test
  public void testThatBranchesMustHaveAValidRegexp() {
    when(this.settings.getString(SETTING_BRANCHES)).thenReturn("[notok");
    this.configValidator.validate(this.settings, this.errors, this.repository);
    assertEquals(SETTING_BRANCHES, on(",").join(this.fieldErrors.keySet()));
    assertEquals(
        "Invalid Regexp: Unclosed character class near index 5 [notok      ^",
        on(",").join(this.fieldErrors.values()));
  }

  @Test
  public void testThatRuleMustHaveAValidRegexp() {
    when(this.settings.getString(SETTING_GROUP_ACCEPT + "[0]"))
        .thenReturn(SbccGroup.Accept.SHOW_MESSAGE.toString());
    when(this.settings.getString(SETTING_GROUP_MATCH + "[0]"))
        .thenReturn(SbccGroup.Match.ALL.toString());
    when(this.settings.getString(SETTING_RULE_REGEXP + "[0][0]")).thenReturn("[notok");
    this.configValidator.validate(this.settings, this.errors, this.repository);
    assertEquals("ruleRegexp[0][0]", on(",").join(this.fieldErrors.keySet()));
    assertEquals(
        "Invalid Regexp: Unclosed character class near index 5 [notok      ^",
        on(",").join(this.fieldErrors.values()));
  }

  @Test
  public void testThatRulesWithAcceptMatchAndRegexpDoesValidate() throws ValidationException {
    when(this.settings.getString(SETTING_GROUP_MATCH + "[0]"))
        .thenReturn(SbccGroup.Match.ALL.toString().toLowerCase());
    when(this.settings.getString(SETTING_GROUP_ACCEPT + "[0]"))
        .thenReturn(SbccGroup.Accept.SHOW_MESSAGE.toString().toLowerCase());
    when(this.settings.getString(SETTING_RULE_REGEXP + "[0][0]")).thenReturn("ok");
    this.configValidator.validate(this.settings, this.errors, this.repository);
    assertEquals("", on(",").join(this.fieldErrors.keySet()));
    assertEquals("", on(",").join(this.fieldErrors.values()));
    assertEquals("ok", sscSettings(this.settings).getGroups().get(0).getRules().get(0).getRegexp());
  }

  @Test
  public void testThatRulesWithoutAcceptDoesNotValidate() {
    when(this.settings.getString(SETTING_GROUP_MATCH + "[0]"))
        .thenReturn(SbccGroup.Match.ALL.toString());
    when(this.settings.getString(SETTING_RULE_REGEXP + "[0][0]")).thenReturn("ok");
    this.configValidator.validate(this.settings, this.errors, this.repository);
    assertEquals(SETTING_GROUP_ACCEPT + "[0]", on(",").join(this.fieldErrors.keySet()));
    assertEquals(
        "Cannot add a rule group without acceptance criteria!",
        on(",").join(this.fieldErrors.values()));
  }

  @Test
  public void testThatRulesWithoutMatchDoesNotValidate() {
    when(this.settings.getString(SETTING_GROUP_ACCEPT + "[0]"))
        .thenReturn(SbccGroup.Accept.SHOW_MESSAGE.toString());
    when(this.settings.getString(SETTING_RULE_REGEXP + "[0][0]")).thenReturn("ok");
    this.configValidator.validate(this.settings, this.errors, this.repository);
    assertEquals(SETTING_GROUP_MATCH + "[0]", on(",").join(this.fieldErrors.keySet()));
    assertEquals(
        "Cannot add a rule group without matching criteria!",
        on(",").join(this.fieldErrors.values()));
  }

  @Test
  public void testThatRulesWithoutRegexpDoesNotValidate() {
    when(this.settings.getString(SETTING_GROUP_ACCEPT + "[0]"))
        .thenReturn(SbccGroup.Accept.SHOW_MESSAGE.toString());
    when(this.settings.getString(SETTING_GROUP_MATCH + "[0]"))
        .thenReturn(SbccGroup.Match.ALL.toString());
    when(this.settings.getString(SETTING_RULE_MESSAGE + "[0][0]")).thenReturn("A Message");
    this.configValidator.validate(this.settings, this.errors, this.repository);
    assertEquals("ruleRegexp[0][0]", on(",").join(this.fieldErrors.keySet()));
    assertEquals("Cannot add a rule without regexp!", on(",").join(this.fieldErrors.values()));
  }

  @Test
  public void testThatValidationDoesNotFailIfNoValuesAreEntered() {
    this.configValidator.validate(this.settings, this.errors, this.repository);
  }
}
