package se.bjurr.sbcc;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.regex.Pattern.compile;
import static se.bjurr.sbcc.SbccCommon.getBitbucketEmail;
import static se.bjurr.sbcc.SbccCommon.getBitbucketName;
import static se.bjurr.sbcc.SbccCommon.getBitbucketUser;
import static se.bjurr.sbcc.SbccCommon.getBitbucketUserSlug;

import java.util.List;
import java.util.regex.Matcher;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.google.common.base.Optional;

import se.bjurr.sbcc.data.SbccChangeSet;

public class SbccRenderer {

  public enum SBCCVariable {
    AUTHOR_EMAIL(
        new Resolver() {
          @Override
          public String resolve(
              AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
            if (sbccChangeSet.isPresent()) {
              return sbccChangeSet.get().getAuthor().getEmailAddress();
            }
            return "";
          }
        }),
    AUTHOR_NAME(
        new Resolver() {
          @Override
          public String resolve(
              AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
            if (sbccChangeSet.isPresent()) {
              return sbccChangeSet.get().getAuthor().getName();
            }
            return "";
          }
        }),
    BITBUCKET_EMAIL(
        new Resolver() {
          @Override
          public String resolve(
              AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
            return getBitbucketEmail(authenticationContext);
          }
        }),
    BITBUCKET_NAME(
        new Resolver() {
          @Override
          public String resolve(
              AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
            return getBitbucketName(authenticationContext);
          }
        }),
    BITBUCKET_USER(
        new Resolver() {
          @Override
          public String resolve(
              AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
            return getBitbucketUser(authenticationContext);
          }
        }),
    BITBUCKET_USER_SLUG(
        new Resolver() {
          @Override
          public String resolve(
              AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
            return getBitbucketUserSlug(authenticationContext);
          }
        }),
    COMMITTER_EMAIL(
        new Resolver() {
          @Override
          public String resolve(
              AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
            if (sbccChangeSet.isPresent()) {
              return sbccChangeSet.get().getCommitter().getEmailAddress();
            }
            return "";
          }
        }),
    COMMITTER_NAME(
        new Resolver() {
          @Override
          public String resolve(
              AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
            if (sbccChangeSet.isPresent()) {
              return sbccChangeSet.get().getCommitter().getName();
            }
            return "";
          }
        }),
    REGEXP(
        new Resolver() {
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

    public String resolve(
        AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
      return this.resolver.resolve(authenticationContext, sbccChangeSet);
    }

    public List<String> resolveAll(String regexp, SbccChangeSet changeSet) {
      return this.resolver.resolveAll(regexp, changeSet);
    }
  }

  private static class Resolver {
    public String resolve(
        AuthenticationContext authenticationContext, Optional<SbccChangeSet> sbccChangeSet) {
      return "";
    }

    public List<String> resolveAll(String regexp, SbccChangeSet changeSet) {
      return newArrayList();
    };
  }

  private final AuthenticationContext authenticationContext;
  private Optional<SbccChangeSet> sbccChangeSet = absent();

  public SbccRenderer(AuthenticationContext authenticationContext) {
    this.authenticationContext = authenticationContext;
  }

  public void append(StringBuilder sb, String renderAndAppend) {
    sb.append(render(renderAndAppend));
  }

  public String render(String string) {
    for (SBCCVariable variable : SBCCVariable.values()) {
      String resolved = variable.resolve(this.authenticationContext, this.sbccChangeSet);
      if (isNullOrEmpty(resolved)) {
        continue;
      }
      string = string.replaceAll("\\$\\{" + variable.name() + "\\}", resolved);
    }
    return string;
  }

  public List<String> renderAll(
      SbccRenderer.SBCCVariable variable,
      String regexp,
      SbccChangeSet sbccChangeSet,
      String toRender) {
    List<String> renderedList = newArrayList();
    for (String resolved : variable.resolveAll(regexp, sbccChangeSet)) {
      renderedList.add(
          render(
              toRender.replaceAll(
                  "\\$\\{" + SbccRenderer.SBCCVariable.REGEXP.name() + "\\}", resolved)));
    }
    return renderedList;
  }

  public void setSbccChangeSet(SbccChangeSet sbccChangeSet) {
    this.sbccChangeSet = fromNullable(sbccChangeSet);
  }
}
