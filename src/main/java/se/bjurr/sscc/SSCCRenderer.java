package se.bjurr.sscc;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.regex.Pattern.compile;
import static se.bjurr.sscc.SSCCCommon.getStashEmail;
import static se.bjurr.sscc.SSCCCommon.getStashName;
import static se.bjurr.sscc.SSCCCommon.getStashUser;

import java.util.List;
import java.util.regex.Matcher;

import se.bjurr.sscc.data.SSCCChangeSet;

import com.atlassian.stash.user.StashAuthenticationContext;
import com.google.common.base.Optional;

public class SSCCRenderer {

 private static class Resolver {
  public String resolve(StashAuthenticationContext stashAuthenticationContext, Optional<SSCCChangeSet> ssccChangeSet) {
   return "";
  }

  public List<String> resolveAll(String regexp, SSCCChangeSet changeSet) {
   return newArrayList();
  };
 }

 public enum SSCCVariable {
  STASH_EMAIL(new Resolver() {
   @Override
   public String resolve(StashAuthenticationContext stashAuthenticationContext, Optional<SSCCChangeSet> ssccChangeSet) {
    return getStashEmail(stashAuthenticationContext);
   }
  }), STASH_NAME(new Resolver() {
   @Override
   public String resolve(StashAuthenticationContext stashAuthenticationContext, Optional<SSCCChangeSet> ssccChangeSet) {
    return getStashName(stashAuthenticationContext);
   }
  }), STASH_USER(new Resolver() {
   @Override
   public String resolve(StashAuthenticationContext stashAuthenticationContext, Optional<SSCCChangeSet> ssccChangeSet) {
    return getStashUser(stashAuthenticationContext);
   }
  }), COMMITTER_NAME(new Resolver() {
   @Override
   public String resolve(StashAuthenticationContext stashAuthenticationContext, Optional<SSCCChangeSet> ssccChangeSet) {
    if (ssccChangeSet.isPresent()) {
     return ssccChangeSet.get().getCommitter().getName();
    }
    return "";
   }
  }), COMMITTER_EMAIL(new Resolver() {
   @Override
   public String resolve(StashAuthenticationContext stashAuthenticationContext, Optional<SSCCChangeSet> ssccChangeSet) {
    if (ssccChangeSet.isPresent()) {
     return ssccChangeSet.get().getCommitter().getEmailAddress();
    }
    return "";
   }
  }), AUTHOR_NAME(new Resolver() {
   @Override
   public String resolve(StashAuthenticationContext stashAuthenticationContext, Optional<SSCCChangeSet> ssccChangeSet) {
    if (ssccChangeSet.isPresent()) {
     return ssccChangeSet.get().getAuthor().getName();
    }
    return "";
   }
  }), AUTHOR_EMAIL(new Resolver() {
   @Override
   public String resolve(StashAuthenticationContext stashAuthenticationContext, Optional<SSCCChangeSet> ssccChangeSet) {
    if (ssccChangeSet.isPresent()) {
     return ssccChangeSet.get().getAuthor().getEmailAddress();
    }
    return "";
   }
  }), REGEXP(new Resolver() {
   @Override
   public List<String> resolveAll(String regexp, SSCCChangeSet changeSet) {
    List<String> allMatches = newArrayList();
    Matcher matcher = compile(regexp).matcher(changeSet.getMessage());
    while (matcher.find()) {
     allMatches.add(matcher.group());
    }
    return allMatches;
   }
  });

  private Resolver resolver;

  private SSCCVariable(Resolver resolver) {
   this.resolver = resolver;
  }

  public String resolve(StashAuthenticationContext stashAuthenticationContext, Optional<SSCCChangeSet> ssccChangeSet) {
   return resolver.resolve(stashAuthenticationContext, ssccChangeSet);
  }

  public List<String> resolveAll(String regexp, SSCCChangeSet changeSet) {
   return resolver.resolveAll(regexp, changeSet);
  }
 }

 private final StashAuthenticationContext stashAuthenticationContext;
 private Optional<SSCCChangeSet> ssccChangeSet = absent();

 public SSCCRenderer(StashAuthenticationContext stashAuthenticationContext) {
  this.stashAuthenticationContext = stashAuthenticationContext;
 }

 public String render(String string) {
  for (SSCCVariable variable : SSCCVariable.values()) {
   string = string.replaceAll("\\$\\{" + variable.name() + "\\}",
     variable.resolve(stashAuthenticationContext, ssccChangeSet));
  }
  return string;
 }

 public List<String> renderAll(SSCCRenderer.SSCCVariable variable, String regexp, SSCCChangeSet ssccChangeSet,
   String toRender) {
  List<String> renderedList = newArrayList();
  for (String resolved : variable.resolveAll(regexp, ssccChangeSet)) {
   renderedList.add(render(toRender.replaceAll("\\$\\{" + SSCCRenderer.SSCCVariable.REGEXP.name() + "\\}", resolved)));
  }
  return renderedList;
 }

 public void append(StringBuilder sb, String renderAndAppend) {
  sb.append(render(renderAndAppend));
 }

 public void setSsccChangeSet(SSCCChangeSet ssccChangeSet) {
  this.ssccChangeSet = fromNullable(ssccChangeSet);
 }
}
