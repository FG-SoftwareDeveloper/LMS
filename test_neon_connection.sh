#!/bin/bash

echo "Testing Neon DB connection with retries..."

# Connection details
URL="jdbc:postgresql://ep-rapid-king-adipnv5l-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslmode=require"
USERNAME="neondb_owner"
PASSWORD="npg_o9JBCIQg0eHP"

echo "URL: $URL"
echo "Username: $USERNAME"
echo "Testing connection..."

# Try connection multiple times to wake up idle database
for i in {1..5}; do
    echo "Attempt $i of 5..."
    
    if psql "$URL" -c "SELECT version();" 2>/dev/null; then
        echo "✅ SUCCESS! Database is active."
        exit 0
    else
        echo "❌ Connection failed. Retrying in 3 seconds..."
        sleep 3
    fi
done

echo "❌ All connection attempts failed. Database may still be idle or credentials may be incorrect."
echo ""
echo "Please:"
echo "1. Go to https://console.neon.tech/"
echo "2. Find your LMS project"  
echo "3. Click 'Resume' or 'Wake up' if status shows 'Idle'"
echo "4. Get fresh connection string if needed"