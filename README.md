# Simple Stash Commit Checker [![Build Status](https://travis-ci.org/tomasbjerre/simple-stash-commit-checker.svg?branch=master)](https://travis-ci.org/tomasbjerre/simple-stash-commit-checker)
Simple, and easy to use, commit checker for Atlassian Stash. There are many commit checkers out there. This plugin aims at being simple and user friendly. The admin GUI ([here](https://raw.githubusercontent.com/tomasbjerre/simple-stash-commit-checker/master/sandbox/admin_upper.png) and [here](https://raw.githubusercontent.com/tomasbjerre/simple-stash-commit-checker/master/sandbox/admin_lower.png)) allows the Stash administrator to add custom messages for each rejection reason. [Here](https://github.com/tomasbjerre/simple-stash-commit-checker/blob/master/src/test/resources/testProdThatRejectResponseLooksGood.txt) is a sample reject message and [here](https://github.com/tomasbjerre/simple-stash-commit-checker/blob/master/src/test/resources/testProdThatSuccessResponseLooksGood.txt) is a sample accept message. [This](https://raw.githubusercontent.com/tomasbjerre/simple-stash-commit-checker/master/sandbox/config_and_reject.png) is a screenshot of a push being rejected.

Available in [Atlassian Marketplace](https://marketplace.atlassian.com/plugins/se.bjurr.sscc.sscc).

## Features
* Check author in commit
 * email is same as users email in Stash.
 * name is same as users name in Stash.
* Check that author name, and/or email, in commit exists for any user in Stash
* Check committer in commit
 * email is same as users email in Stash.
 * name is same as users name in Stash.
* Optionally check author email against regular expression instead of equality to email in Stash. Like:
 * ^${STASH_USER}@.*
 * ^[^@]*@company.domain$
* Check that changed content does not match a specific regexp, like unresolved merge.
* Check size of commits, so that large files don't accidently gets pushed.
* Check JQL query. Can be used to check that any JIRA is in a specific state. There is an extra variable, ${REGEXP}, available for use in the query.
 * Example: issue = ${REGEXP} AND status = "In Progress" AND assignee in ("${STASH_USER}")
* Simple configuration of rules that must apply to commit messages. Organized in groups.
 * A group can be used for matching, for example, issues. It can state that "at least one", "all of" or "none" of the issues can be mentioned in the commit messages.
 * Rules are added to the group. A rule can, for example, define Jira as a regular expression and the name "Jira".
 * If a group matches a commit, it can reject it or just show a message to the comitter.
* Check only branches matching a regular expression.
* Check that branch name matches specific regexp.
* Exclude merge commits.
* Exclude tag commits.
* Check commits in pull requests
* Show a general reject message.
* Show a general accept message.
* Optionally accept all commits from service users.
* Dry run mode, where all commits are accepted. But verification results are shown.

## Design goals
The included features should:

* Be easy to configure as an administrator of Stash. Any rejection reason delivered to the committer should be configurable.
* Be easy to use as a committer. With user friendly rejection messages, that clearly explains what was wrong and how to fix it.

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
