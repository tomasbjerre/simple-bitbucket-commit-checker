# Simple Stash Commit Checker [![Build Status](https://travis-ci.org/tomasbjerre/simple-stash-commit-checker.svg?branch=master)](https://travis-ci.org/tomasbjerre/simple-stash-commit-checker)
Simple, and easy to use, commit checker for Atlassian Stash. There are many commit checkers out there. This plugin aims at being simple and user friendly. The [admin GUI](https://raw.githubusercontent.com/tomasbjerre/simple-stash-commit-checker/master/sandbox/admin_gui.png) allows the Stash administrator to add custom messages for each rejection reason. [Here](https://github.com/tomasbjerre/simple-stash-commit-checker/blob/master/src/test/resources/testProdThatRejectResponseLooksGood.txt) is a sample reject message and [here](https://github.com/tomasbjerre/simple-stash-commit-checker/blob/master/src/test/resources/testProdThatSuccessResponseLooksGood.txt) is a sample accept message. [This](https://raw.githubusercontent.com/tomasbjerre/simple-stash-commit-checker/master/sandbox/config_and_reject.png) is a screenshot of a push being rejected.

The [ChangeSetsServiceImpl](https://github.com/tomasbjerre/simple-stash-commit-checker/blob/master/src/main/java/se/bjurr/sscc/ChangeSetsServiceImpl.java) is more or less taken from [YACC](https://github.com/sford/yet-another-commit-checker). Which is a great commit checker that you may consider for more advanced features.

Available in [Atlassian Marketplace](https://marketplace.atlassian.com/plugins/se.bjurr.sscc.sscc).

## Features
* It can check that email in Stash is same as in commit.
* It can check that name in Stash is same as in commit.
* Simple configuration of rules that must apply to commit messages. Organized in groups.
 * A group can be used for matching, for example, issues. It can state that "at least one", "all of" or "none" of the issues can be mentioned in the commit messages.
 * Rules are added to the group. A rule can, for example, define Jira as a regular expression and the name "Jira".
 * If a group matches a commit, it can reject it or just show a message to the comitter.
* Check only branches matching a regular expression.
* Dry run mode, where all commits are accepted. But verification results are shown.
* Exclude merge commits.
* Exclude tag commits.
* Show a general reject message.
* Show a general accept message.

## Design goals
The included features should:

* Be easy to configure as an administrator of Stash. Any rejection reason delivered to the committer should be configurable.
* Be easy to use as a committer. With user friendly rejection messages, that clearly explains what was wrong and how to fix it.
* Be well tested. If a feature cannot be easily tested, it should not be added.

If too much effort is put into restricting commit messages, chances are the checker will grow too complex. It will be hard to maintain and users wont trust its features. For example this checker will never check any referred issues in other systems to make sure their status is correct. Such an integration is just waste of time, committers will find a way around it anyway. Instead the goal here is to inform the committer about any faulty commits and how to correct them.

## Developer instructions
You will need Atlas SDK to compile the code.

https://developer.atlassian.com/docs/getting-started/set-up-the-atlassian-plugin-sdk-and-build-a-project

You can generate Eclipse project:
```
atlas-compile eclipse:eclipse
```

Package the plugin:
```
atlas-package
```

Run Stash, with the plugin, on localhost:
```
export MAVEN_OPTS=-Dplugin.resource.directories=`pwd`/src/main/resources
atlas-run
```

Make a release:

https://developer.atlassian.com/docs/common-coding-tasks/development-cycle/packaging-and-releasing-your-plugin
```
mvn release:prepare
mvn release:perform
```
