package se.bjurr.sbcc.commits;

import static com.google.common.base.Joiner.on;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.data.SbccPerson;
import se.bjurr.sbcc.settings.SbccSettings;

import com.atlassian.bitbucket.io.LineReader;
import com.atlassian.bitbucket.io.LineReaderOutputHandler;
import com.atlassian.bitbucket.scm.CommandOutputHandler;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;

public class RevListOutputHandler extends LineReaderOutputHandler
    implements CommandOutputHandler<List<SbccChangeSet>> {
  private static Logger logger = LoggerFactory.getLogger(RevListOutputHandler.class);

  private static final String RAW_BODY = "%B";
  private static final String NEW_LINE = "%n";
  private static final String COMMITTER_EMAIL = "%cE";
  private static final String COMMITTER_NAME = "%cN";
  private static final String AUTHOR_EMAIL = "%aE";
  private static final String AUTHOR_NAME = "%aN";
  private static final String PARENT_HASH = "%P";
  private static final String START_OF_TEXT = "%x02";
  private static final String COMMIT_HASH = "%H";
  private static final String END = "%x03END%x04";
  /** https://git-scm.com/docs/git-rev-list */
  public static final String FORMAT = //
      COMMIT_HASH
          + START_OF_TEXT
          + // 0
          PARENT_HASH
          + START_OF_TEXT
          + // 1
          COMMITTER_NAME
          + START_OF_TEXT
          + // 2
          COMMITTER_EMAIL
          + START_OF_TEXT
          + // 3
          AUTHOR_NAME
          + START_OF_TEXT
          + // 4
          AUTHOR_EMAIL
          + NEW_LINE
          + // 5
          RAW_BODY
          + NEW_LINE
          + END; // 6

  private static final String OUTPUT_END = "\u0003END\u0004";
  private static final String OUTPUT_NEW_LINE = "\u0002";

  private final List<SbccChangeSet> commits = new ArrayList<>();
  private final SbccSettings settings;

  public RevListOutputHandler(SbccSettings settings) {
    super(Charsets.UTF_8);
    this.settings = settings;
  }

  @VisibleForTesting
  RevListOutputHandler() {
    super(Charsets.UTF_8);
    settings = null;
  }

  @Nullable
  @Override
  public List<SbccChangeSet> getOutput() {
    return commits;
  }

  @Override
  protected void processReader(LineReader lineReader) throws IOException {
    settings.shouldExcludeMergeCommits();

    final List<String> lines = new ArrayList<>();
    String line;
    while ((line = lineReader.readLine()) != null) {
      lines.add(line);
    }

    final Iterator<String> linesItr = lines.iterator();
    final List<SbccChangeSet> newCommits =
        getSbccChangeSet(linesItr, settings.shouldExcludeMergeCommits());
    commits.addAll(newCommits);
  }

  @VisibleForTesting
  List<SbccChangeSet> getSbccChangeSet(
      Iterator<String> linesItr, boolean shouldExcludeMergeCommits) {
    final List<SbccChangeSet> commits = new ArrayList<>();
    while (linesItr.hasNext()) {
      String line = linesItr.next();
      if (!line.startsWith("commit ")) {
        throw new RuntimeException("unexpected line: " + line);
      }

      line = linesItr.next();

      final String[] commitData = line.split(OUTPUT_NEW_LINE);
      try {
        final String ref = commitData[0];

        final boolean isMerge = commitData[1].contains(" ");

        if (isMerge && shouldExcludeMergeCommits) {
          while (linesItr.hasNext()
              && (line = linesItr.next()) != null
              && !line.equals(OUTPUT_END)) {
            // Read the rest of this object before continuing with next object
          }
          continue;
        }

        final String committerName = commitData[2];
        final String committerEmail = commitData[3];
        final SbccPerson committer = new SbccPerson(committerName, committerEmail);

        final String authorName = commitData[4];
        final String authorEmail = commitData[5];
        final SbccPerson author = new SbccPerson(authorName, authorEmail);

        final String message = parseMessage(linesItr);

        final SbccChangeSet sbccChangeSet =
            changeSetBuilder() //
                .withCommitter(committer) //
                .withAuthor(author) //
                .withId(ref) //
                .withMessage(message) //
                .build();
        commits.add(sbccChangeSet);
      } catch (final Exception e) {
        logger.error("Unable to parse commit, commit data found:\n" + on('\n').join(commitData), e);
      }
    }
    return commits;
  }

  private String parseMessage(Iterator<String> linesItr) throws IOException {
    String message = "";

    String line;
    while (linesItr.hasNext() && (line = linesItr.next()) != null && !line.equals(OUTPUT_END)) {
      if (!message.isEmpty()) {
        message += "\n";
      }

      message += line.trim();
    }

    return message.trim();
  }
}
