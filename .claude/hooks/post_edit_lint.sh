#!/bin/bash
# VehicleNote - PostToolUse 린트 훅
# Edit 또는 Write 도구로 .kt 파일이 수정된 후 자동 실행됩니다.
# 컴파일 에러를 즉시 감지해 AI가 같은 컨텍스트에서 수정할 수 있도록 피드백합니다.

# CLAUDE_TOOL_INPUT 에서 file_path 추출
FILE_PATH=$(python3 -c "
import sys, json, os
raw = os.environ.get('CLAUDE_TOOL_INPUT', '{}')
try:
    d = json.loads(raw)
    print(d.get('file_path', ''))
except:
    print('')
" 2>/dev/null)

# .kt 파일이 아니면 스킵
if [[ "$FILE_PATH" != *.kt ]]; then
  exit 0
fi

# 프로젝트 루트로 이동 (git 루트 기준)
PROJECT_ROOT=$(git -C "$(dirname "$FILE_PATH")" rev-parse --show-toplevel 2>/dev/null)
if [[ -z "$PROJECT_ROOT" ]]; then
  exit 0
fi

cd "$PROJECT_ROOT" || exit 0

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "[훅] Kotlin 파일 수정 감지: $(basename "$FILE_PATH")"
echo "[훅] 컴파일 검사 실행 중..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 컴파일 실행 (에러·경고 라인만 필터링)
OUTPUT=$(./gradlew :app:compileDebugKotlin 2>&1)
EXIT_CODE=$?

# error 라인 추출
ERRORS=$(echo "$OUTPUT" | grep -E "^.*\.kt:[0-9]+:[0-9]+: error:" | head -20)
WARNINGS=$(echo "$OUTPUT" | grep -E "^.*\.kt:[0-9]+:[0-9]+: warning:" | head -10)
BUILD_LINE=$(echo "$OUTPUT" | grep -E "^BUILD (SUCCESSFUL|FAILED)" | tail -1)

if [[ $EXIT_CODE -ne 0 ]]; then
  echo "❌ 컴파일 실패"
  if [[ -n "$ERRORS" ]]; then
    echo ""
    echo "[ 에러 목록 ]"
    echo "$ERRORS"
  fi
  # 전체 에러 컨텍스트 (마지막 40줄)
  echo ""
  echo "[ 상세 로그 (마지막 40줄) ]"
  echo "$OUTPUT" | tail -40
  echo ""
  echo "⚠️  위 에러를 수정한 후 다시 시도하세요."
  exit 1
else
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
fi
