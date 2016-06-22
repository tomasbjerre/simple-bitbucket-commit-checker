package se.bjurr.sbcc;

import static com.google.common.base.Optional.absent;
import static java.util.regex.Pattern.DOTALL;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.settings.SbccSettings;

import com.google.common.base.Optional;

public class CommitContentValidator {
 private final SbccSettings settings;

 public CommitContentValidator(SbccSettings settings) {
  this.settings = settings;
 }

 public SbccSettings getSettings() {
  return this.settings;
 }

 public Optional<String> validateChangeSetForContentDiff(SbccChangeSet sbccChangeSet) {
  if (!this.settings.getCommitDiffRegexp().isPresent()) {
   return absent();
  }
  Matcher m = Pattern.compile(this.settings.getCommitDiffRegexp().get(), DOTALL).matcher(sbccChangeSet.getDiff());
  if (m.find()) {
   return Optional.of(sbccChangeSet.getDiff().substring(m.start(), m.end()));
  }
  return absent();
 }

 public Map<String, Long> validateChangeSetForContentSize(SbccChangeSet sbccChangeSet) {
  return sbccChangeSet.getSizeAboveLimitPerFile();
 }
}
