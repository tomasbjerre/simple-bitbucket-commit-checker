package se.bjurr.sbcc.commits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Before;
import org.junit.Test;

public class RevListOutputHandlerTest {
  private RevListOutputHandler sut;
private Git git;
private Repository repo;

  @Before
  public void before() throws IOException {
    sut = new RevListOutputHandler();
    repo =         new FileRepositoryBuilder() //
            .readEnvironment() //
            .findGitDir() //
            .build();
	this.git = new Git(repo);
  }

  @Test
  public void testIgnoreMergeCommits() throws Exception {
	  final AnyObjectId since = ObjectId.fromString("067804308b6e87738ec6f68a57d57316eeb361a0");
	final AnyObjectId until = ObjectId.fromString("b8b570cebd6637204f9e9b53baff480e8580fb65");
	final Iterable<RevCommit> call = git//
			.log()//
			.addRange(since, until)
			.call();
	for (final RevCommit r : call) {
		System.out.println(r.getName());
	}

    final List<String> lines = new ArrayList<>();
    final boolean shouldExcludeMergeCommits = false;
    sut.getSbccChangeSet(lines.iterator(), shouldExcludeMergeCommits);
  }
}
