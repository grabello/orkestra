#!/usr/bin/env bash
set -euo pipefail

AWS_ENDPOINT="http://localhost:4566"
AWS_REGION="us-east-1"
TABLE_NAME="local_orkestra_platform_admins"

export AWS_ACCESS_KEY_ID="test"
export AWS_SECRET_ACCESS_KEY="test"
export AWS_DEFAULT_REGION="$AWS_REGION"

USER_ID="${1:?Usage: seed-platform-admin.sh <supabase-sub> <email>}"
EMAIL="${2:?Usage: seed-platform-admin.sh <supabase-sub> <email>}"

aws --endpoint-url="$AWS_ENDPOINT" dynamodb put-item \
  --table-name "$TABLE_NAME" \
  --item "{
    \"userId\": {\"S\": \"$USER_ID\"},
    \"email\": {\"S\": \"$EMAIL\"},
    \"status\": {\"S\": \"ACTIVE\"},
    \"createdAt\": {\"S\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}
  }"
