package se.bjurr.sbcc;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.regex.Pattern.compile;
import static se.bjurr.sbcc.SbccCommon.getBitbucketEmail;
import static se.bjurr.sbcc.SbccCommon.getBitbucketName;
import static se.bjurr.sbcc.SbccCommon.getBitbucketUser;

import java.util.List;
import java.util.regex.Matcher;

import se.bjurr.sbcc.data.SbccChangeSet;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.google.common.base.Optional;

public class SbccRenderer {

 private static class Resolver {
  public String resolve(AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
   return "";
  }

  public List<String> resolveAll(String regexp, SbccChangeSet changeSet) {
   return newArrayList();
  };
 }

 public enum SBCCVariable {
  BITBUCKET_EMAIL(new Resolver() {
   @Override
   public String resolve(AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
    return getBitbucketEmail(authenticationContext);
   }
  }), BITBUCKET_NAME(new Resolver() {
   @Override
   public String resolve(AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
    return getBitbucketName(authenticationContext);
   }
  }), BITBUCKET_USER(new Resolver() {
   @Override
   public String resolve(AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
    return getBitbucketUser(authenticationContext);
   }
  }), COMMITTER_NAME(new Resolver() {
   @Override
   public String resolve(AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
    if (sbccChangeSet.isPresent()) {
     return sbccChangeSet.get().getCommitter().getName();
    }
    return "";
   }
  }), COMMITTER_EMAIL(new Resolver() {
   @Override
   public String resolve(AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
    if (sbccChangeSet.isPresent()) {
     return sbccChangeSet.get().getCommitter().getEmailAddress();
    }
    return "";
   }
  }), AUTHOR_NAME(new Resolver() {
   @Override
   public String resolve(AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
    if (sbccChangeSet.isPresent()) {
     return sbccChangeSet.get().getAuthor().getName();
    }
    return "";
   }
  }), AUTHOR_EMAIL(new Resolver() {
   @Override
   public String resolve(AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
    if (sbccChangeSet.isPresent()) {
     return sbccChangeSet.get().getAuthor().getEmailAddress();
    }
    return "";
   }
  }), REGEXP(new Resolver() {
   @Override
   public List<String> resolveAll(String regexp, SbccChangeSet changeSet) {
    List<String> allMatches = newArrayList();
    Matcher matcher = compile(regexp).matcher(changeSet.getMessage());
    while (matcher.find()) {
     allMatches.add(matcher.group());
    }
    return allMatches;
   }
  });

  private Resolver resolver;

  private SBCCVariable(Resolver resolver) {
   this.resolver = resolver;
  }

  public String resolve(AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
   return resolver.resolve(authenticationContext, sbccChangeSet);
  }

  public List<String> resolveAll(String regexp, SbccChangeSet changeSet) {
   return resolver.resolveAll(regexp, changeSet);
  }
 }

 private final AuthenticationContext authenticationContext;
 private Optional<SbccChangeSet> sbccChangeSet = absent();

 public SbccRenderer(AuthenticationContext authenticationContext) {
  this.authenticationContext = authenticationContext;
 }

 public String render(String string) {
  for (SBCCVariable variable : SBCCVariable.values()) {
   string = string.replaceAll("\\$\\{" + variable.name() + "\\}",
     variable.resolve(authenticationContext, sbccChangeSet));
  }
  return string;
 }

 public List<String> renderAll(SbccRenderer.SBCCVariable variable, String regexp, SbccChangeSet sbccChangeSet,
   String toRender) {
  List<String> renderedList = newArrayList();
  for (String resolved : variable.resolveAll(regexp, sbccChangeSet)) {
   renderedList.add(render(toRender.replaceAll("\\$\\{" + SbccRenderer.SBCCVariable.REGEXP.name() + "\\}", resolved)));
  }
  return renderedList;
 }

 public void append(StringBuilder sb, String renderAndAppend) {
  sb.append(render(renderAndAppend));
 }

 public void setSbccChangeSet(SbccChangeSet sbccChangeSet) {
  this.sbccChangeSet = fromNullable(sbccChangeSet);
 }
}
