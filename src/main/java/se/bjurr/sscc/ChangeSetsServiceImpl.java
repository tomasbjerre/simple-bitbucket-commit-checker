package se.bjurr.sscc;

import static com.atlassian.stash.repository.RefChangeType.DELETE;
import static com.atlassian.stash.scm.git.GitRefPattern.TAGS;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.jgit.lib.ObjectId.fromString;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import se.bjurr.sscc.data.SSCCChangeSet;
import se.bjurr.sscc.data.SSCCPerson;
import se.bjurr.sscc.settings.SSCCSettings;

import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.content.ChangesetsBetweenRequest;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageProvider;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PagedIterable;
import com.google.common.collect.Sets;

public class ChangeSetsServiceImpl implements ChangeSetsService {
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
 public List<SSCCChangeSet> getNewChangeSets(SSCCSettings settings, Repository repository, RefChange refChange)
   throws IOException {
  final org.eclipse.jgit.lib.Repository jGitRepo = getJGitRepo(repository);

  final RevWalk walk = new RevWalk(jGitRepo);

  final List<SSCCChangeSet> changesets = newArrayList();

  if (refChange.getRefId().startsWith(TAGS.getPath())) {
   if (settings.shouldExcludeTagCommits()) {
    return changesets;
   }
   if (refChange.getType() == DELETE) {
    return changesets;
   }

   final RevObject obj = walk.parseAny(fromString(refChange.getToHash()));
   if (!(obj instanceof RevTag)) {
    return changesets;
   }

   final RevTag tag = (RevTag) obj;

   final PersonIdent ident = tag.getTaggerIdent();
   final String message = tag.getFullMessage();
   final SSCCPerson committer = new SSCCPerson(ident.getName(), ident.getEmailAddress());
   final SSCCChangeSet changeset = new SSCCChangeSet(refChange.getToHash(), committer, message, 1);

   changesets.add(changeset);
  } else {
   final ChangesetsBetweenRequest request = new ChangesetsBetweenRequest.Builder(repository)
     .exclude(getBranches(repository)).include(refChange.getToHash()).build();

   final Iterable<Changeset> changes = new PagedIterable<Changeset>(new PageProvider<Changeset>() {
    @Override
    public Page<Changeset> get(PageRequest pr) {
     return commitService.getChangesetsBetween(request, pr);
    }
   }, 100);

   for (final Changeset changeset : changes) {
    final RevCommit commit = walk.parseCommit(fromString(changeset.getId()));

    final PersonIdent ident = commit.getCommitterIdent();
    final String message = commit.getFullMessage();
    final SSCCPerson committer = new SSCCPerson(ident.getName(), ident.getEmailAddress());

    changesets.add(new SSCCChangeSet(changeset.getId(), committer, message, commit.getParentCount()));
   }
  }

  return changesets;
 }
}
