package se.bjurr.sbcc.commits;

import static com.google.common.base.Joiner.on;
import static se.bjurr.sbcc.data.SbccChangeSetBuilder.changeSetBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bitbucket.io.LineReader;
import com.atlassian.bitbucket.io.LineReaderOutputHandler;
import com.atlassian.bitbucket.scm.CommandOutputHandler;

import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.data.SbccPerson;
import se.bjurr.sbcc.settings.SbccSettings;

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
    super(Charset.forName("UTF-8"));
    this.settings = settings;
  }

  @Nullable
  @Override
  public List<SbccChangeSet> getOutput() {
    return commits;
  }

  @Override
  protected void processReader(LineReader lineReader) throws IOException {
    String line;
    while ((line = lineReader.readLine()) != null) {

      if (!line.startsWith("commit ")) {
        throw new RuntimeException("unexpected line: " + line);
      }

      line = lineReader.readLine();

      String[] commitData = line.split(OUTPUT_NEW_LINE);
      try {
        String ref = commitData[0];

        boolean isMerge = commitData[1].contains(" ");

        if (isMerge && settings.shouldExcludeMergeCommits()) {
          while ((line = lineReader.readLine()) != null && !line.equals(OUTPUT_END)) {
            // Read the rest of this object before continuing with next object
          }
          continue;
        }

        String committerName = commitData[2];
        String committerEmail = commitData[3];
        SbccPerson committer = new SbccPerson(committerName, committerEmail);

        String authorName = commitData[4];
        String authorEmail = commitData[5];
        SbccPerson author = new SbccPerson(authorName, authorEmail);

        String message = parseMessage(lineReader);

        SbccChangeSet sbccChangeSet =
            changeSetBuilder() //
                .withCommitter(committer) //
                .withAuthor(author) //
                .withId(ref) //
                .withMessage(message) //
                .build();
        commits.add(sbccChangeSet);
      } catch (Exception e) {
        logger.error("Unable to parse commit, commit data found:\n" + on('\n').join(commitData), e);
      }
    }
  }

  private String parseMessage(LineReader lineReader) throws IOException {
    String message = "";

    String line;
    while ((line = lineReader.readLine()) != null && !line.equals(OUTPUT_END)) {
      if (!message.isEmpty()) {
        message += "\n";
      }

      message += line.trim();
    }

    return message.trim();
  }
}
