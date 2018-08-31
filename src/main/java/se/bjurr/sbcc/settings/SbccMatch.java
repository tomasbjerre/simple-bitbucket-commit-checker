package se.bjurr.sbcc.settings;

import java.util.List;
import se.bjurr.sbcc.settings.SbccGroup.Match;

public class SbccMatch {
  private final Match match;
  private final List<SbccRule> matchingRules;

  public SbccMatch(Match match, List<SbccRule> matchingRules) {
    this.matchingRules = matchingRules;
    this.match = match;
  }

  public Match getMatch() {
    return match;
  }

  public List<SbccRule> getMatchingRules() {
    return matchingRules;
  }
}
