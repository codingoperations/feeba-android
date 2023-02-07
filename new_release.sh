#!/bin/bash
currTag="$(git describe --tags $(git rev-list --tags --max-count=1))"
echo "currTag: ${currTag}"
IFS=. read major minor patch <<< "$currTag"

echo "major: ${major}"
echo "minor: ${minor}"
echo "patch: ${patch}"

newTag="${major}.${minor}.${patch}"
echo "Old tag: ${newTag}"

if [[ $1 == "major" ]]; then
     newTag="$((major + 1)).0.0"
elif [[ $1 == "minor" ]]; then
     newTag="${major}.$((minor + 1)).0"
else
     newTag="${major}.${minor}.$((patch + 1))"
fi
echo "New tag ${newTag}"

git tag "$newTag"
git push origin tag "$newTag"