package se.bjurr.sscc.settings;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import java.util.List;

import com.google.common.base.Optional;

public class SSCCGroup implements Comparable<SSCCGroup> {
 public enum Accept {
  /**
   * Accept if matching.
   */
  ACCEPT,
  /**
   * Just show the message, don't accept or reject
   */
  SHOW_MESSAGE
 }

 public enum Match {
  /**
   * If all rules are matching
   */
  ALL,
  /**
   * If no rule is matching
   */
  NONE,
  /**
   * If at least one rule is matching
   */
  ONE;
 }

 public static SSCCGroup ssccGroup() {
  return new SSCCGroup();
 }

 private Accept accept;
 private Match match;
 private String message;
 private List<SSCCRule> rules;

 private SSCCGroup() {
 }

 @Override
 public int compareTo(SSCCGroup o) {
  return toString().compareTo(o.toString());
 }

 public Accept getAccept() {
  return accept;
 }

 public Match getMatch() {
  return match;
 }

 public Optional<String> getMessage() {
  return fromNullable(message);
 }

 public List<SSCCRule> getRules() {
  return rules;
 }

 @Override
 public String toString() {
  return accept.toString() + " " + match.toString() + " " + message;
 }

 public SSCCGroup withAccept(String accept) {
  checkNotNull(accept, "No acceptance rule specified!");
  this.accept = Accept.valueOf(accept.toUpperCase());
  return this;
 }

 public SSCCGroup withMatch(String match) {
  checkNotNull(match, "No match rule specified!");
  this.match = Match.valueOf(match.toUpperCase());
  return this;
 }

 public SSCCGroup withMessage(String message) {
  this.message = emptyToNull(message);
  return this;
 }

 public SSCCGroup withRules(List<SSCCRule> rules) {
  checkNotNull(rules, "Cannot set null rules");
  this.rules = rules;
  return this;
 }
}
