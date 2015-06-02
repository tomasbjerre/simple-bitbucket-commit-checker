# Simple Stash Commit Checker Changelog

Changelog of Simple Stash Commit Checker

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

