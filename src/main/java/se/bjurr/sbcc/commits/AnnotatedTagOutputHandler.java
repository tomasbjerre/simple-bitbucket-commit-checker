package se.bjurr.sbcc.commits;

import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.atlassian.bitbucket.io.LineReader;
import com.atlassian.bitbucket.io.LineReaderOutputHandler;
import com.atlassian.bitbucket.scm.CommandOutputHandler;

import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.data.SbccPerson;

public class AnnotatedTagOutputHandler extends LineReaderOutputHandler
    implements CommandOutputHandler<SbccChangeSet> {

  private SbccChangeSet sbccChangeSet = null;

  public AnnotatedTagOutputHandler(final String ref) {
    super(Charset.forName("UTF-8"));
  }

  @Nullable
  @Override
  public SbccChangeSet getOutput() {
    return sbccChangeSet;
  }

  @Override
  protected void processReader(final LineReader lineReader) throws IOException {
    String line;

    SbccPerson tagger = new SbccPerson("", "");
    boolean isTag = false;
    String message = null;
    String ref = null;

    while ((line = lineReader.readLine()) != null) {
      if (line.startsWith("tag ")) {
        isTag = true;
        ref = line;
      } else if (line.startsWith("tagger ")) {
        tagger = parseTagger(line);
      } else if (line.isEmpty()) {
        message = parseMessage(lineReader);
      }
    }

    if (isTag) {
      sbccChangeSet =
          changeSetBuilder() //
              .withCommitter(tagger) //
              .withAuthor(tagger) //
              .withId(ref) //
              .withMessage(message) //'
              .withTag(true) //
              .build();
    }
  }

  private SbccPerson parseTagger(final String line) {
    final Pattern pattern = Pattern.compile("^tagger (.*)\\s*<([^>]*)> .*$");
    final Matcher matcher = pattern.matcher(line);
    if (matcher.matches()) {
      final String name = matcher.group(1).trim();
      final String email = matcher.group(2).trim();
      return new SbccPerson(name, email);
    } else {
      return new SbccPerson("", "");
    }
  }

  private String parseMessage(final LineReader lineReader) throws IOException {
    final StringBuilder message = new StringBuilder();

    String line;
    while ((line = lineReader.readLine()) != null) {
      message.append(line + "\n");
    }

    return message.toString();
  }
}
