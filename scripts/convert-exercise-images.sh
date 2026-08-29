#!/usr/bin/env bash
# Resizes every PNG under a given exercise's asset folder to a phone-friendly size, in place.
# Usage: scripts/convert-exercise-images.sh <exercise-slug> [size]
# Example: scripts/convert-exercise-images.sh chest-opener 640
set -euo pipefail
SLUG="${1:?exercise slug required, e.g. chest-opener}"
SIZE="${2:-640}"
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/app/src/main/assets/images/exercises/${SLUG}"

[ -d "$DIR" ] || { echo "no such folder: $DIR" >&2; exit 1; }

for f in "$DIR"/*.png; do
  [ -e "$f" ] || continue
  sips --resampleHeightWidth "$SIZE" "$SIZE" "$f" >/dev/null
  echo "resized $f to ${SIZE}x${SIZE}"
done
