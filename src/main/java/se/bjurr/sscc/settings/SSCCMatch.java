package se.bjurr.sscc.settings;

import java.util.List;

import se.bjurr.sscc.settings.SSCCGroup.Match;

public class SSCCMatch {
 private final Match match;
 private final List<SSCCRule> matchingRules;

 public SSCCMatch(Match match, List<SSCCRule> matchingRules) {
  this.matchingRules = matchingRules;
  this.match = match;
 }

 public Match getMatch() {
  return match;
 }

 public List<SSCCRule> getMatchingRules() {
  return matchingRules;
 }
}
