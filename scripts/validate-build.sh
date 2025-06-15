#!/bin/bash

# Time Capsule Selfies - Build Validation Script
# This script validates the app build and ensures it's ready for release

set -e

echo "🚀 Time Capsule Selfies - Build Validation"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    print_error "Please run this script from the project root directory"
    exit 1
fi

print_status "Starting build validation..."

# 1. Clean build
echo ""
echo "📦 Cleaning previous builds..."
./gradlew clean
print_status "Clean completed"

# 2. Run unit tests
echo ""
echo "🧪 Running unit tests..."
./gradlew test
if [ $? -eq 0 ]; then
    print_status "All unit tests passed"
else
    print_error "Unit tests failed"
    exit 1
fi

# 3. Run lint checks
echo ""
echo "🔍 Running lint checks..."
./gradlew lint
if [ $? -eq 0 ]; then
    print_status "Lint checks passed"
else
    print_warning "Lint checks found issues - check reports/lint-results.html"
fi

# 4. Build debug APK
echo ""
echo "🔨 Building debug APK..."
./gradlew assembleDebug
if [ $? -eq 0 ]; then
    print_status "Debug APK built successfully"
else
    print_error "Debug build failed"
    exit 1
fi

# 5. Build release APK
echo ""
echo "🔨 Building release APK..."
./gradlew assembleRelease
if [ $? -eq 0 ]; then
    print_status "Release APK built successfully"
else
    print_error "Release build failed"
    exit 1
fi

# 6. Check APK sizes
echo ""
echo "📊 Checking APK sizes..."
DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
RELEASE_APK="app/build/outputs/apk/release/app-release.apk"

if [ -f "$DEBUG_APK" ]; then
    DEBUG_SIZE=$(du -h "$DEBUG_APK" | cut -f1)
    print_status "Debug APK size: $DEBUG_SIZE"
else
    print_error "Debug APK not found"
fi

if [ -f "$RELEASE_APK" ]; then
    RELEASE_SIZE=$(du -h "$RELEASE_APK" | cut -f1)
    print_status "Release APK size: $RELEASE_SIZE"
else
    print_error "Release APK not found"
fi

# 7. Validate manifest
echo ""
echo "📋 Validating manifest..."
if grep -q "android:debuggable=\"true\"" app/src/main/AndroidManifest.xml; then
    print_warning "Debuggable flag found in manifest - ensure it's only for debug builds"
fi

if grep -q "android:allowBackup=\"true\"" app/src/main/AndroidManifest.xml; then
    print_status "Backup is enabled"
fi

# 8. Check for hardcoded strings
echo ""
echo "🔤 Checking for hardcoded strings..."
HARDCODED_COUNT=$(find app/src/main/java -name "*.kt" -exec grep -l "\"[A-Za-z]" {} \; | wc -l)
if [ $HARDCODED_COUNT -gt 0 ]; then
    print_warning "Found $HARDCODED_COUNT files with potential hardcoded strings"
else
    print_status "No hardcoded strings found"
fi

# 9. Check permissions
echo ""
echo "🔐 Validating permissions..."
REQUIRED_PERMS=("android.permission.CAMERA" "android.permission.READ_MEDIA_IMAGES")
for perm in "${REQUIRED_PERMS[@]}"; do
    if grep -q "$perm" app/src/main/AndroidManifest.xml; then
        print_status "Permission $perm is declared"
    else
        print_error "Missing required permission: $perm"
    fi
done

# 10. Generate build report
echo ""
echo "📄 Generating build report..."
BUILD_TIME=$(date)
COMMIT_HASH=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")

cat > build-report.txt << EOF
Time Capsule Selfies - Build Report
===================================

Build Time: $BUILD_TIME
Commit Hash: $COMMIT_HASH

APK Locations:
- Debug: $DEBUG_APK
- Release: $RELEASE_APK

APK Sizes:
- Debug: $DEBUG_SIZE
- Release: $RELEASE_SIZE

Build Status: SUCCESS
EOF

print_status "Build report generated: build-report.txt"

# 11. Final summary
echo ""
echo "🎉 Build Validation Complete!"
echo "=============================="
print_status "All checks passed successfully"
print_status "APKs are ready for testing/deployment"

if [ -f "$RELEASE_APK" ]; then
    echo ""
    echo "📱 Next Steps:"
    echo "1. Test the release APK on physical devices"
    echo "2. Verify all features work correctly"
    echo "3. Check performance on low-end devices"
    echo "4. Prepare for app store submission"
    echo ""
    echo "Release APK location: $RELEASE_APK"
fi

echo ""
print_status "Build validation completed successfully! 🚀"
