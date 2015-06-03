package se.bjurr.sscc;

import static se.bjurr.sscc.SSCCCommon.getStashEmail;
import static se.bjurr.sscc.SSCCCommon.getStashName;
import static se.bjurr.sscc.SSCCCommon.getStashUser;

import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.user.StashAuthenticationContext;

public class SSCCRenderer {

 public interface Resolver {
  public String resolve(StashAuthenticationContext stashAuthenticationContext);
 }

 public enum SSCCVariable {
  STASH_EMAIL(new Resolver() {
   @Override
   public String resolve(StashAuthenticationContext stashAuthenticationContext) {
    return getStashEmail(stashAuthenticationContext);
   }
  }), STASH_NAME(new Resolver() {
   @Override
   public String resolve(StashAuthenticationContext stashAuthenticationContext) {
    return getStashName(stashAuthenticationContext);
   }
  }), STASH_USER(new Resolver() {
   @Override
   public String resolve(StashAuthenticationContext stashAuthenticationContext) {
    return getStashUser(stashAuthenticationContext);
   }
  });

  private Resolver resolver;

  private SSCCVariable(Resolver resolver) {
   this.resolver = resolver;
  }

  public String resolve(StashAuthenticationContext stashAuthenticationContext) {
   return resolver.resolve(stashAuthenticationContext);
  }
 }

 private final HookResponse hookResponse;
 private final StashAuthenticationContext stashAuthenticationContext;

 public SSCCRenderer(StashAuthenticationContext stashAuthenticationContext, HookResponse hookResponse) {
  this.stashAuthenticationContext = stashAuthenticationContext;
  this.hookResponse = hookResponse;
 }

 public void println() {
  hookResponse.out().println();
 }

 public void println(String string) {
  hookResponse.out().println(render(string));
 }

 public String render(String string) {
  for (SSCCVariable variable : SSCCVariable.values()) {
   string = string.replaceAll("\\$\\{" + variable.name() + "\\}", variable.resolve(stashAuthenticationContext));
  }
  return string;
 }
}
