#!/bin/bash
# ./review-pr.sh <number>: checks out branch review/<head> holding pull request <number>'s files, with HEAD moved
# back to the commit the pull request forked from, so its changes show as uncommitted for review in the IDE.
set -euo pipefail
pr=$1

if [ -n "$(git status --porcelain)" ]; then
  echo "The working tree has changes; commit or stash them first." >&2
  exit 1
fi

read -r head base < <(gh pr view "$pr" --json headRefName,baseRefName -q '.headRefName + " " + .baseRefName')
url=https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner).git

set -x
git fetch "$url" "+$head:refs/remotes/origin/$head" "+$base:refs/remotes/origin/$base"
git checkout -b "review/$head" "origin/$head"
git reset "$(git merge-base HEAD "origin/$base")"
git ls-files -z --others --exclude-standard | xargs -0 -r git add -N
