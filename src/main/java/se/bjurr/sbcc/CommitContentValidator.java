package se.bjurr.sbcc;

import static com.google.common.base.Optional.absent;
import static com.google.common.collect.Maps.newTreeMap;
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
  return settings;
 }

 public Map<String, Long> validateChangeSetForContentSize(SbccChangeSet sbccChangeSet) {
  Map<String, Long> exceeding = newTreeMap();
  for (String file : sbccChangeSet.getSizePerFile().keySet()) {
   Long sizeKb = sbccChangeSet.getSizePerFile().get(file) / 1024;
   if (sizeKb > settings.getCommitSizeKb()) {
    exceeding.put(file, sizeKb);
   }
  }
  return exceeding;
 }

 public Optional<String> validateChangeSetForContentDiff(SbccChangeSet sbccChangeSet) {
  if (!settings.getCommitDiffRegexp().isPresent()) {
   return absent();
  }
  Matcher m = Pattern.compile(settings.getCommitDiffRegexp().get(), DOTALL).matcher(sbccChangeSet.getDiff());
  if (m.find()) {
   return Optional.of(sbccChangeSet.getDiff().substring(m.start(), m.end()));
  }
  return absent();
 }
}
