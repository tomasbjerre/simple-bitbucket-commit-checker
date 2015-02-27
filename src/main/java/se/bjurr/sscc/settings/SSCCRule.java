package se.bjurr.sscc.settings;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import com.google.common.base.Optional;

public class SSCCRule {
 public static SSCCRule ssccRule() {
  return new SSCCRule();
 }

 private String message;
 private String regexp;

 private SSCCRule() {
 }

 public Optional<String> getMessage() {
  return fromNullable(message);
 }

 public String getRegexp() {
  return regexp;
 }

 public SSCCRule withMessage(String ruleMessage) {
  this.message = emptyToNull(ruleMessage);
  return this;
 }

 public SSCCRule withRegexp(String ruleRegexp) {
  checkNotNull(ruleRegexp, "Regexp rule must be set!");
  this.regexp = ruleRegexp;
  return this;
 }
}
