package se.bjurr.sbcc.settings;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import com.google.common.base.Optional;
import java.util.List;

public class SbccGroup implements Comparable<SbccGroup> {
  public enum Accept {
    /** Accept if matching. */
    ACCEPT,
    /** Just show the message, don't accept or reject */
    SHOW_MESSAGE
  }

  public enum Match {
    /** If all rules are matching */
    ALL,
    /** If no rule is matching */
    NONE,
    /** If at least one rule is matching */
    ONE;
  }

  public static SbccGroup sbccGroup() {
    return new SbccGroup();
  }

  private Accept accept;
  private Match match;
  private String message;
  private List<SbccRule> rules;

  private SbccGroup() {}

  @Override
  public int compareTo(SbccGroup o) {
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

  public List<SbccRule> getRules() {
    return rules;
  }

  @Override
  public String toString() {
    return accept.toString() + " " + match.toString() + " " + message;
  }

  public SbccGroup withAccept(String accept) {
    checkNotNull(accept, "No acceptance rule specified!");
    this.accept = Accept.valueOf(accept.toUpperCase());
    return this;
  }

  public SbccGroup withMatch(String match) {
    checkNotNull(match, "No match rule specified!");
    this.match = Match.valueOf(match.toUpperCase());
    return this;
  }

  public SbccGroup withMessage(String message) {
    this.message = emptyToNull(message);
    return this;
  }

  public SbccGroup withRules(List<SbccRule> rules) {
    checkNotNull(rules, "Cannot set null rules");
    this.rules = rules;
    return this;
  }
}
