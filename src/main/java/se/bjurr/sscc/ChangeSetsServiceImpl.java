package se.bjurr.sscc;

import static com.atlassian.stash.repository.RefChangeType.DELETE;
import static com.atlassian.stash.scm.git.GitRefPattern.TAGS;
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

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.data.SSCCPerson;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.content.ChangesetsBetweenRequest;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageProvider;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PagedIterable;
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
 public List<SSCCChangeSet> getNewChangeSets(SSCCSettings settings, Repository repository, String refId,
   RefChangeType type, String fromHash, String toHash) throws IOException {
  final ChangesetsBetweenRequest changesetsBetweenRequest = new ChangesetsBetweenRequest.Builder(repository)
    .exclude(getBranches(repository)).include(toHash).build();
  return getNewChangesets(settings, repository, refId, type, toHash, changesetsBetweenRequest);
 }

 @Override
 public List<SSCCChangeSet> getNewChangeSets(SSCCSettings settings, PullRequest pullRequest) throws IOException {
  final ChangesetsBetweenRequest changesetsBetweenRequest = new ChangesetsBetweenRequest.Builder(pullRequest).build();
  return getNewChangesets(settings, pullRequest.getToRef().getRepository(), pullRequest.getToRef().getId(),
    RefChangeType.ADD, pullRequest.getToRef().getLatestChangeset(), changesetsBetweenRequest);
 }

 private List<SSCCChangeSet> getNewChangesets(SSCCSettings settings, Repository repository, String refId,
   RefChangeType type, String toHash, final ChangesetsBetweenRequest request) throws IOException,
   MissingObjectException, IncorrectObjectTypeException, CorruptObjectException {
  final org.eclipse.jgit.lib.Repository jGitRepo = getJGitRepo(repository);

  final RevWalk walk = new RevWalk(jGitRepo);

  final List<SSCCChangeSet> changesets = newArrayList();

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
   final SSCCPerson committer = new SSCCPerson(ident.getName(), ident.getEmailAddress());
   changesets.add(new SSCCChangeSet(toHash, committer, committer, message, 1, new HashMap<String, Long>(), ""));
  } else {
   final Iterable<Changeset> changes = new PagedIterable<Changeset>(new PageProvider<Changeset>() {
    @Override
    public Page<Changeset> get(PageRequest pr) {
     return commitService.getChangesetsBetween(request, pr);
    }
   }, 100);

   for (final Changeset changeset : changes) {
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
     final SSCCPerson author = new SSCCPerson(authorIdent.getName(), authorIdent.getEmailAddress());
     final PersonIdent committerIdent = commit.getCommitterIdent();
     final SSCCPerson committer = new SSCCPerson(committerIdent.getName(), committerIdent.getEmailAddress());
     changesets.add(new SSCCChangeSet(changeset.getId(), committer, author, message, commit.getParentCount(),
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
   ObjectLoader loader = jGitRepo.open(treeWalk.getObjectId(0));
   fileSizes.put(treeWalk.getPathString(), loader.getSize());
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
