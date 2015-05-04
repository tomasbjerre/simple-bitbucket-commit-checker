package se.bjurr.sscc;

import static com.google.common.base.Optional.absent;
import static java.util.regex.Pattern.DOTALL;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.settings.SSCCSettings;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class CommitContentValidator {
 private final SSCCSettings settings;

 public CommitContentValidator(SSCCSettings settings) {
  this.settings = settings;
 }

 public SSCCSettings getSettings() {
  return settings;
 }

 public Map<String, Long> validateChangeSetForContentSize(SSCCChangeSet ssccChangeSet) {
  Map<String, Long> exceeding = Maps.newHashMap();
  for (String file : ssccChangeSet.getSizePerFile().keySet()) {
   Long sizeKb = ssccChangeSet.getSizePerFile().get(file) / 1024;
   if (sizeKb > settings.getCommitSizeKb()) {
    exceeding.put(file, sizeKb);
   }
  }
  return exceeding;
 }

 public Optional<String> validateChangeSetForContentDiff(SSCCChangeSet ssccChangeSet) {
  if (!settings.getCommitDiffRegexp().isPresent()) {
   return absent();
  }
  Matcher m = Pattern.compile(settings.getCommitDiffRegexp().get(), DOTALL).matcher(ssccChangeSet.getDiff());
  if (m.find()) {
   return Optional.of(ssccChangeSet.getDiff().substring(m.start(), m.end()));
  }
  return absent();
 }
}
