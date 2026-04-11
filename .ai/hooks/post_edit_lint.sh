#!/bin/bash
# VehicleNote - shared AI post-edit lint hook
# Claude and Codex should both route Kotlin edit checks through this script.

extract_file_path() {
  local raw="$1"
  python3 -c '
import json, os, sys
raw = sys.argv[1] if len(sys.argv) > 1 else ""
if not raw:
    print("")
    raise SystemExit(0)
try:
    data = json.loads(raw)
except Exception:
    print("")
    raise SystemExit(0)

def pick(obj):
    if isinstance(obj, dict):
        for key in ("file_path", "path", "target_file"):
            value = obj.get(key)
            if isinstance(value, str) and value:
                return value
        for key in ("input", "tool_input", "payload"):
            value = obj.get(key)
            result = pick(value)
            if result:
                return result
    elif isinstance(obj, list):
        for item in obj:
            result = pick(item)
            if result:
                return result
    return ""

print(pick(data))
' "$raw" 2>/dev/null
}

FILE_PATH=""

for ENV_NAME in CLAUDE_TOOL_INPUT CODEX_TOOL_INPUT TOOL_INPUT; do
  VALUE="${!ENV_NAME}"
  if [[ -n "$VALUE" ]]; then
    FILE_PATH=$(extract_file_path "$VALUE")
    if [[ -n "$FILE_PATH" ]]; then
      break
    fi
  fi
done

if [[ -z "$FILE_PATH" && -n "$1" ]]; then
  FILE_PATH="$1"
fi

if [[ "$FILE_PATH" != *.kt ]]; then
  exit 0
fi

PROJECT_ROOT=$(git -C "$(dirname "$FILE_PATH")" rev-parse --show-toplevel 2>/dev/null)
if [[ -z "$PROJECT_ROOT" ]]; then
  exit 0
fi

cd "$PROJECT_ROOT" || exit 0

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "[훅] Kotlin 파일 수정 감지: $(basename "$FILE_PATH")"
echo "[훅] 공통 컴파일 검사 실행 중..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

OUTPUT=$(./gradlew :app:compileDebugKotlin 2>&1)
EXIT_CODE=$?

ERRORS=$(echo "$OUTPUT" | grep -E "^.*\\.kt:[0-9]+:[0-9]+: error:" | head -20)
WARNINGS=$(echo "$OUTPUT" | grep -E "^.*\\.kt:[0-9]+:[0-9]+: warning:" | head -10)
BUILD_LINE=$(echo "$OUTPUT" | grep -E "^BUILD (SUCCESSFUL|FAILED)" | tail -1)

if [[ $EXIT_CODE -ne 0 ]]; then
  echo "❌ 컴파일 실패"
  if [[ -n "$ERRORS" ]]; then
    echo ""
    echo "[ 에러 목록 ]"
    echo "$ERRORS"
  fi
  echo ""
  echo "[ 상세 로그 (마지막 40줄) ]"
  echo "$OUTPUT" | tail -40
  echo ""
  echo "⚠️  위 에러를 수정한 후 다시 시도하세요."
  exit 1
fi

echo "✅ $BUILD_LINE"
if [[ -n "$WARNINGS" ]]; then
  echo ""
  echo "[ 경고 (참고용) ]"
  echo "$WARNINGS"
fi
echo ""
echo "📋 docs/ 문서 최신화가 필요한지 확인하세요."
echo "   수정한 화면이 있다면 → docs/0X_화면명.md 업데이트"
exit 0
