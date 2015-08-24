# Simple Stash Commit Checker Changelog

Changelog of Simple Stash Commit Checker

## 1.12
* Fixing bug:
 * Checking branch names, even if no commits are pushed.

## 1.11
* Fixing bug:
 * "An error occured" is shown every time users see a Pull Request #41

## 1.10
* Checking commits in pull requests
* Fixing bugs:
 * Author name and email can be checked in Stash, was using wrong key in GUI to store the setting.
 * "Require Matching Author Email" does not need to be checked for email regexp matching to work.

## 1.9
* Check that author name, and/or email, in commit exists for any user in Stash

## 1.8
* Check JQL query. Can be used to check that any JIRA is in a specific state. There is an extra variable, ${REGEXP}, available for use in the query.
 * Example: issue = ${REGEXP} AND status = "In Progress" AND assignee in ("${STASH_USER}")

## 1.7
* Optionally check author email against regular expression instead of equality to email in Stash. Like:
 * ^${STASH_USER}@.*
 * ^.*@company.domain$

## 1.6
* Correctly reporting if committer or author name was faulty

## 1.5
* Reject commits if regexp does not match branch name
* Check that committer equals Stash user
* Allow service users to by pass the hook

## 1.4
* Checking author against author instead of committer. Committer is the one pushing the commit.

## 1.3
* Ignoring merge commits at lowest level, where tag commits are already ignored.
* Ability to block commits:
 * Containing files larger then a configurable size.
 * With content matching specific regexp, like unresolved merge.

## 1.2
* Marking it as compatible with Stash Data Center

## 1.1

* Fixing bugs
 * Checking "Display Name" from Stash instead of "Name"
* Adding features
 * Making ${STASH_EMAIL} and ${STASH_NAME} variables available in configurable messages 
 * Printing name and version of plugin, in responses
* Other changes
 * Cleaning the code

## 1.0

* Initial Release

