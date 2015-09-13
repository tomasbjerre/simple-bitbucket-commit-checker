package se.bjurr.sbcc.settings;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import com.google.common.base.Optional;

public class SbccRule {
 public static SbccRule sbccRule() {
  return new SbccRule();
 }

 private String message;
 private String regexp;

 private SbccRule() {
 }

 public Optional<String> getMessage() {
  return fromNullable(message);
 }

 public String getRegexp() {
  return regexp;
 }

 public SbccRule withMessage(String ruleMessage) {
  this.message = emptyToNull(ruleMessage);
  return this;
 }

 public SbccRule withRegexp(String ruleRegexp) {
  checkNotNull(ruleRegexp, "Regexp rule must be set!");
  this.regexp = ruleRegexp;
  return this;
 }
}
