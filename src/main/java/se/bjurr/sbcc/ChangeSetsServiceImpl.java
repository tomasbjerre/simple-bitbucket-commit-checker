package se.bjurr.sbcc;

import static com.atlassian.bitbucket.repository.RefChangeType.DELETE;
import static com.atlassian.bitbucket.scm.git.GitRefPattern.TAGS;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;
import static java.util.logging.Level.SEVERE;
import static org.eclipse.jgit.lib.ObjectId.fromString;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import se.bjurr.sbcc.data.SbccChangeSet;
import se.bjurr.sbcc.data.SbccPerson;
import se.bjurr.sbcc.settings.SbccSettings;

import com.atlassian.bitbucket.commit.Commit;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.commit.CommitsBetweenRequest;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageProvider;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.bitbucket.util.PagedIterable;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

public class ChangeSetsServiceImpl implements ChangeSetsService {
 private static Logger logger = Logger.getLogger(ChangeSetsServiceImpl.class.getName());
 private final ApplicationPropertiesService applicationPropertiesService;
 private final CommitService commitService;

 public ChangeSetsServiceImpl(CommitService commitService, ApplicationPropertiesService applicationPropertiesService) {
  this.commitService = commitService;
  this.applicationPropertiesService = applicationPropertiesService;
 }

 private Set<String> getBranches(Repository repository) throws IOException {
  final org.eclipse.jgit.lib.Repository jGitRepo = getJGitRepo(repository);

  final Set<String> refHeads = Sets.newHashSet();

  for (final String ref : jGitRepo.getAllRefs().keySet()) {
   if (ref.startsWith("refs/heads/")) {
    refHeads.add(ref);
   }
  }

  return refHeads;
 }

 private org.eclipse.jgit.lib.Repository getJGitRepo(Repository repository) throws IOException {
  final FileRepositoryBuilder builder = new FileRepositoryBuilder();
  final File repoDir = applicationPropertiesService.getRepositoryDir(repository);
  return builder.setGitDir(repoDir).build();
 }

 @Override
 public List<SbccChangeSet> getNewChangeSets(SbccSettings settings, Repository repository, String refId,
   RefChangeType type, String fromHash, String toHash) throws IOException {
  final CommitsBetweenRequest changesetsBetweenRequest = new CommitsBetweenRequest.Builder(repository)
    .exclude(getBranches(repository)).include(toHash).build();
  return getNewChangesets(settings, repository, refId, type, toHash, changesetsBetweenRequest);
 }

 @Override
 public List<SbccChangeSet> getNewChangeSets(SbccSettings settings, PullRequest pullRequest) throws IOException {
  final CommitsBetweenRequest changesetsBetweenRequest = new CommitsBetweenRequest.Builder(pullRequest).build();
  return getNewChangesets(settings, pullRequest.getToRef().getRepository(), pullRequest.getToRef().getId(),
    RefChangeType.ADD, pullRequest.getToRef().getLatestCommit(), changesetsBetweenRequest);
 }

 private List<SbccChangeSet> getNewChangesets(SbccSettings settings, Repository repository, String refId,
   RefChangeType type, String toHash, final CommitsBetweenRequest request) throws IOException, MissingObjectException,
   IncorrectObjectTypeException, CorruptObjectException {
  final org.eclipse.jgit.lib.Repository jGitRepo = getJGitRepo(repository);

  final RevWalk walk = new RevWalk(jGitRepo);

  final List<SbccChangeSet> changesets = newArrayList();

  if (refId.startsWith(TAGS.getPath())) {
   if (settings.shouldExcludeTagCommits()) {
    return changesets;
   }
   if (type == DELETE) {
    return changesets;
   }

   final RevObject obj = walk.parseAny(fromString(toHash));
   if (!(obj instanceof RevTag)) {
    return changesets;
   }

   final RevTag tag = (RevTag) obj;

   final String message = tag.getFullMessage();
   final PersonIdent ident = tag.getTaggerIdent();
   final SbccPerson committer = new SbccPerson(ident.getName(), ident.getEmailAddress());
   changesets.add(new SbccChangeSet(toHash, committer, committer, message, 1, new HashMap<String, Long>(), ""));
  } else {
   final Iterable<Commit> changes = new PagedIterable<Commit>(new PageProvider<Commit>() {
    @Override
    public Page<Commit> get(PageRequest pr) {
     return commitService.getCommitsBetween(request, pr);
    }
   }, 100);

   for (final Commit changeset : changes) {
    if (changeset.getParents().size() > 1 && settings.shouldExcludeMergeCommits()) {
     continue;
    }

    try {
     final RevCommit commit = walk.parseCommit(fromString(changeset.getId()));
     Optional<RevCommit> firstParentCommit = Optional.absent();
     if (changeset.getParents().size() > 0) {
      // If this is not the very first commit in the repo
      firstParentCommit = Optional.of(walk.parseCommit(fromString(changeset.getParents().iterator().next().getId())));
     }

     String diff = getDiffString(jGitRepo, commit, firstParentCommit);

     Map<String, Long> sizePerFile = newHashMap();
     if (settings.shouldCheckCommitSize()) {
      sizePerFile = getSizePerFile(jGitRepo, commit);
     }

     final String message = commit.getFullMessage();
     final PersonIdent authorIdent = commit.getAuthorIdent();
     final SbccPerson author = new SbccPerson(authorIdent.getName(), authorIdent.getEmailAddress());
     final PersonIdent committerIdent = commit.getCommitterIdent();
     final SbccPerson committer = new SbccPerson(committerIdent.getName(), committerIdent.getEmailAddress());
     changesets.add(new SbccChangeSet(changeset.getId(), committer, author, message, commit.getParentCount(),
       sizePerFile, diff));
    } catch (GitAPIException e) {
     logger.log(SEVERE, refId, e);
    }
   }
  }

  return changesets;
 }

 private Map<String, Long> getSizePerFile(final org.eclipse.jgit.lib.Repository jGitRepo, final RevCommit commit)
   throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
  Map<String, Long> fileSizes = newTreeMap();
  RevTree tree = commit.getTree();
  TreeWalk treeWalk = new TreeWalk(jGitRepo);
  treeWalk.addTree(tree);
  treeWalk.setRecursive(true);
  while (treeWalk.next()) {
   AnyObjectId objectId = treeWalk.getObjectId(0);
   if (jGitRepo.hasObject(objectId)) {
    ObjectLoader loader = jGitRepo.open(objectId);
    fileSizes.put(treeWalk.getPathString(), loader.getSize());
   }
  }
  return fileSizes;
 }

 private String getDiffString(final org.eclipse.jgit.lib.Repository jGitRepo, final RevCommit commit,
   Optional<RevCommit> firstParentCommit) throws IncorrectObjectTypeException, IOException, GitAPIException {
  if (!firstParentCommit.isPresent()) {
   return "";
  }
  ObjectReader reader = jGitRepo.newObjectReader();
  CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
  oldTreeIter.reset(reader, firstParentCommit.get().getTree().getId());
  CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
  newTreeIter.reset(reader, commit.getTree().getId());

  final StringBuilder sb = new StringBuilder();
  OutputStream out = new OutputStream() {
   @Override
   public void write(int b) throws IOException {
    sb.append((char) b);
   }
  };
  new Git(jGitRepo).diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).setOutputStream(out).call();
  return sb.toString();
 }
}
