#!/usr/bin/env bash
# 文本级换皮辅助：替换 Java 包前缀与 artifact 片段。先 git commit / 备份再跑。
set -euo pipefail

NEW_PACKAGE="${1:-}"
NEW_ARTIFACT="${2:-}"

if [[ -z "$NEW_PACKAGE" || -z "$NEW_ARTIFACT" ]]; then
  echo "Usage: $0 <new.package.name> <new-artifact-id>"
  echo "Example: $0 com.acme.erp acme-erp"
  exit 1
fi

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OLD_PACKAGE="com.omni.scaffolding"
OLD_ARTIFACT="omni-scaffolding"
OLD_ARTIFACT_SHORT="omni"

echo "Root: $ROOT"
echo "Package: $OLD_PACKAGE -> $NEW_PACKAGE"
echo "Artifact: $OLD_ARTIFACT -> $NEW_ARTIFACT"
echo "WARNING: This is a best-effort text replace. Review diff before commit."
read -r -p "Continue? [y/N] " ok
[[ "$ok" == "y" || "$ok" == "Y" ]] || exit 0

# package path segments
OLD_PATH="${OLD_PACKAGE//.//}"
NEW_PATH="${NEW_PACKAGE//.//}"

# 1) file contents
find "$ROOT" \
  \( -path "$ROOT/.git" -o -path "$ROOT/omni-web/node_modules" -o -path "$ROOT/**/target" \) -prune -o \
  -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.yaml" -o -name "*.md" \
    -o -name "*.ftl" -o -name "*.ts" -o -name "*.vue" -o -name "*.properties" -o -name "pom.xml" \) \
  -print0 | while IFS= read -r -d '' f; do
  if grep -q "$OLD_PACKAGE\|$OLD_ARTIFACT\|$OLD_ARTIFACT_SHORT-scaffolding" "$f" 2>/dev/null; then
    sed -i.bak \
      -e "s/${OLD_PACKAGE//./\\.}/${NEW_PACKAGE}/g" \
      -e "s/${OLD_ARTIFACT}/${NEW_ARTIFACT}/g" \
      "$f"
    rm -f "${f}.bak"
  fi
done

# 2) move java directories (common case: com/omni/scaffolding)
move_tree() {
  local base="$1"
  local src="$base/src"
  [[ -d "$src" ]] || return 0
  find "$src" -type d -path "*/${OLD_PATH}" 2>/dev/null | while read -r dir; do
    parent="$(dirname "$dir")"
    dest="$parent/$NEW_PATH"
    mkdir -p "$(dirname "$dest")"
    if [[ -d "$dest" ]]; then
      echo "Skip existing: $dest"
    else
      mkdir -p "$dest"
      # move children then remove empty old dirs
      shopt -s dotglob nullglob
      mv "$dir"/* "$dest"/ 2>/dev/null || true
      rmdir "$dir" 2>/dev/null || true
      echo "Moved: $dir -> $dest"
    fi
  done
}

for mod in omni-common omni-framework omni-modules omni-demo omni-quartz omni-admin; do
  move_tree "$ROOT/$mod"
done

echo "Done. Next:"
echo "  mvn -s .mvn/settings.xml -pl omni-admin -am compile -DskipTests"
echo "  Review remaining 'omni' names (module folders, Docker service names) manually."
