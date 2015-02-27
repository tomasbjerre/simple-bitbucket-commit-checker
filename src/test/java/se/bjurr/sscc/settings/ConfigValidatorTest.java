package se.bjurr.sscc.settings;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_BRANCHES;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_ACCEPT;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_GROUP_MATCH;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_RULE_MESSAGE;
import static se.bjurr.sscc.settings.SSCCSettings.SETTING_RULE_REGEXP;
import static se.bjurr.sscc.settings.SSCCSettings.sscSettings;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import se.bjurr.sscc.ConfigValidator;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;

public class ConfigValidatorTest {
 private ConfigValidator configValidator;
 private final SettingsValidationErrors errors = new SettingsValidationErrors() {
  @Override
  public void addFieldError(String field, String error) {
   fieldErrors.put(field, error);
  }

  @Override
  public void addFormError(String error) {
   formError.add(error);
  }
 };
 private final Map<String, String> fieldErrors = newHashMap();
 private final List<String> formError = newArrayList();
 private Repository repository;
 private Settings settings;

 @Before
 public void before() {
  settings = mock(Settings.class);
  when(settings.getBoolean(anyString())).thenReturn(null);
  when(settings.getString(anyString())).thenReturn(null);
  configValidator = new ConfigValidator();
 }

 @Test
 public void testThatBranchesCanBeEmpty() {
  when(settings.getString(SETTING_BRANCHES)).thenReturn("");
  configValidator.validate(settings, errors, repository);
  assertEquals("", on(",").join(fieldErrors.keySet()));
  assertEquals("", on(",").join(fieldErrors.values()));
 }

 @Test
 public void testThatBranchesMustHaveAValidRegexp() {
  when(settings.getString(SETTING_BRANCHES)).thenReturn("[notok");
  configValidator.validate(settings, errors, repository);
  assertEquals(SETTING_BRANCHES, on(",").join(fieldErrors.keySet()));
  assertEquals("Invalid Regexp: Unclosed character class near index 5 [notok      ^", on(",")
    .join(fieldErrors.values()));
 }

 @Test
 public void testThatRuleMustHaveAValidRegexp() {
  when(settings.getString(SETTING_GROUP_ACCEPT + "[0]")).thenReturn(SSCCGroup.Accept.SHOW_MESSAGE.toString());
  when(settings.getString(SETTING_GROUP_MATCH + "[0]")).thenReturn(SSCCGroup.Match.ALL.toString());
  when(settings.getString(SETTING_RULE_REGEXP + "[0][0]")).thenReturn("[notok");
  configValidator.validate(settings, errors, repository);
  assertEquals("ruleRegexp[0][0]", on(",").join(fieldErrors.keySet()));
  assertEquals("Invalid Regexp: Unclosed character class near index 5 [notok      ^", on(",")
    .join(fieldErrors.values()));
 }

 @Test
 public void testThatRulesWithAcceptMatchAndRegexpDoesValidate() throws ValidationException {
  when(settings.getString(SETTING_GROUP_MATCH + "[0]")).thenReturn(SSCCGroup.Match.ALL.toString().toLowerCase());
  when(settings.getString(SETTING_GROUP_ACCEPT + "[0]")).thenReturn(
    SSCCGroup.Accept.SHOW_MESSAGE.toString().toLowerCase());
  when(settings.getString(SETTING_RULE_REGEXP + "[0][0]")).thenReturn("ok");
  configValidator.validate(settings, errors, repository);
  assertEquals("", on(",").join(fieldErrors.keySet()));
  assertEquals("", on(",").join(fieldErrors.values()));
  assertEquals("ok", sscSettings(settings).getGroups().get(0).getRules().get(0).getRegexp());
 }

 @Test
 public void testThatRulesWithoutAcceptDoesNotValidate() {
  when(settings.getString(SETTING_GROUP_MATCH + "[0]")).thenReturn(SSCCGroup.Match.ALL.toString());
  when(settings.getString(SETTING_RULE_REGEXP + "[0][0]")).thenReturn("ok");
  configValidator.validate(settings, errors, repository);
  assertEquals(SETTING_GROUP_ACCEPT + "[0]", on(",").join(fieldErrors.keySet()));
  assertEquals("Cannot add a rule group without acceptance criteria!", on(",").join(fieldErrors.values()));
 }

 @Test
 public void testThatRulesWithoutMatchDoesNotValidate() {
  when(settings.getString(SETTING_GROUP_ACCEPT + "[0]")).thenReturn(SSCCGroup.Accept.SHOW_MESSAGE.toString());
  when(settings.getString(SETTING_RULE_REGEXP + "[0][0]")).thenReturn("ok");
  configValidator.validate(settings, errors, repository);
  assertEquals(SETTING_GROUP_MATCH + "[0]", on(",").join(fieldErrors.keySet()));
  assertEquals("Cannot add a rule group without matching criteria!", on(",").join(fieldErrors.values()));
 }

 @Test
 public void testThatRulesWithoutRegexpDoesNotValidate() {
  when(settings.getString(SETTING_GROUP_ACCEPT + "[0]")).thenReturn(SSCCGroup.Accept.SHOW_MESSAGE.toString());
  when(settings.getString(SETTING_GROUP_MATCH + "[0]")).thenReturn(SSCCGroup.Match.ALL.toString());
  when(settings.getString(SETTING_RULE_MESSAGE + "[0][0]")).thenReturn("A Message");
  configValidator.validate(settings, errors, repository);
  assertEquals("ruleRegexp[0][0]", on(",").join(fieldErrors.keySet()));
  assertEquals("Cannot add a rule without regexp!", on(",").join(fieldErrors.values()));
 }

 @Test
 public void testThatValidationDoesNotFailIfNoValuesAreEntered() {
  configValidator.validate(settings, errors, repository);
 }
}
