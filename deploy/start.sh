#!/bin/bash
# ==========================================
# DealXanh - Production Startup Script (Linux/Mac)
# ==========================================

APP_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_FILE="$APP_DIR/DealXanh.jar"
CONFIG_DIR="$APP_DIR/config/"
UPLOAD_DIR="$APP_DIR/uploads/"

# --- Tao thu muc uploads ---
mkdir -p "$UPLOAD_DIR"

# --- Java (yeu cau JDK 17+) ---
JAVA="${JAVA_HOME:-/usr/bin/java}"

echo "=========================================="
echo "  DealXanh Server"
echo "  Jar:    $JAR_FILE"
echo "  Config: $CONFIG_DIR"
echo "=========================================="

# --- Chay ung dung ---
cd "$APP_DIR"
exec "$JAVA" \
    -Xms256m -Xmx512m \
    --enable-native-access=ALL-UNNAMED \
    -Dfile.encoding=UTF-8 \
    -Dspring.config.additional-location=file:./config/ \
    -jar "$JAR_FILE"
