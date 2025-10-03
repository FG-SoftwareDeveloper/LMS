#!/bin/bash

# Script to get current Neon DB connection details using API
# You'll need to set these environment variables from your GitHub secrets:
# NEON_API_KEY and NEON_PROJECT_ID

if [ -z "$NEON_API_KEY" ] || [ -z "$NEON_PROJECT_ID" ]; then
    echo "Please set NEON_API_KEY and NEON_PROJECT_ID environment variables"
    echo "You can find these in your GitHub repository secrets"
    exit 1
fi

echo "Fetching current Neon DB connection details..."

# Get project details
curl -X GET \
  "https://console.neon.tech/api/v2/projects/$NEON_PROJECT_ID" \
  -H "Authorization: Bearer $NEON_API_KEY" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "Fetching connection details..."

# Get connection details for the main branch
curl -X GET \
  "https://console.neon.tech/api/v2/projects/$NEON_PROJECT_ID/connection_uri" \
  -H "Authorization: Bearer $NEON_API_KEY" \
  -H "Content-Type: application/json"