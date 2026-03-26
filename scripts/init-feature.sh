#!/bin/bash

# Geohod Backend Feature Initialization Script
# Usage: ./scripts/init-feature.sh <feature-name> [description]
#
# Example: ./scripts/init-feature.sh user-statistics "Add comprehensive user statistics functionality"

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if feature name is provided
if [ -z "$1" ]; then
    print_error "Feature name is required as the first argument"
    echo "Usage: $0 <feature-name> [description]"
    echo "Example: $0 user-statistics 'Add comprehensive user statistics functionality'"
    exit 1
fi

FEATURE_NAME="$1"
FEATURE_BRANCH="feature/${FEATURE_NAME}"
DESCRIPTION="${2:-Add $FEATURE_NAME functionality}"

print_info "Initializing feature: $FEATURE_NAME"
print_info "Description: $DESCRIPTION"
print_info "Feature branch: $FEATURE_BRANCH"

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    print_error "Not in a git repository. Please run this script from the project root."
    exit 1
fi

# Check if the branch already exists
if git show-ref --verify --quiet "refs/heads/$FEATURE_BRANCH"; then
    print_warning "Branch $FEATURE_BRANCH already exists."
    read -p "Do you want to switch to it? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git checkout "$FEATURE_BRANCH"
        print_success "Switched to existing branch: $FEATURE_BRANCH"
    else
        print_info "Operation cancelled."
        exit 0
    fi
else
    # Create and switch to new feature branch
    print_info "Creating new feature branch: $FEATURE_BRANCH"
    git checkout -b "$FEATURE_BRANCH"
    print_success "Created and switched to branch: $FEATURE_BRANCH"
fi

# Create feature specification file
SPEC_FILE="${FEATURE_NAME^^}_FEATURE_SPEC.md"
SPEC_PATH="$SPEC_FILE"

if [ ! -f "$SPEC_PATH" ]; then
    print_info "Creating feature specification file: $SPEC_PATH"

    cat > "$SPEC_PATH" << EOF
# ${FEATURE_NAME^} Feature Specification

## Overview
$DESCRIPTION

## Feature Status
⏳ **IN DEVELOPMENT** - Feature branch created, implementation pending.

## API Endpoints
TBD - Define API endpoints here

## Service Layer
TBD - Define service interfaces and implementations here

## Data Access Layer
TBD - Define repository changes and database schema here

## Controller Implementation
TBD - Define controller endpoints and request/response DTOs here

## Error Handling
TBD - Define error scenarios and exception handling here

## Performance Considerations
TBD - Define performance requirements and optimization strategies here

## Security
TBD - Define authentication and authorization requirements here

## Testing
TBD - Define testing strategy and coverage requirements here

## API Documentation
TBD - Define OpenAPI/Swagger documentation requirements here

## Future Enhancements
TBD - Define potential future improvements and extensions here

## Deployment Notes
TBD - Define deployment requirements and database migrations here

## Dependencies
TBD - Define internal and external dependencies here

## File Structure
TBD - Define expected file structure for this feature here

## Integration Points
TBD - Define integration points with other features here

## Success Criteria
TBD - Define acceptance criteria and success metrics here

## Maintenance Notes
TBD - Define maintenance considerations and operational requirements here

## Implementation Checklist
- [ ] Service interface defined
- [ ] Service implementation completed
- [ ] Repository methods implemented
- [ ] Controller endpoints created
- [ ] DTOs defined and mapped
- [ ] Error handling implemented
- [ ] Input validation added
- [ ] Unit tests written
- [ ] Integration tests written
- [ ] API documentation updated
- [ ] Database migrations created (if needed)
- [ ] Feature tested end-to-end

## Notes
- Created on: $(date -u +"%Y-%m-%dT%H:%M:%SZ")
- Feature branch: $FEATURE_BRANCH
- Status: Initial specification created, implementation pending
EOF

    print_success "Created feature specification: $SPEC_PATH"
else
    print_warning "Specification file already exists: $SPEC_PATH"
fi

# Create feature directory structure if needed
FEATURE_DIR_LOWER=$(echo "$FEATURE_NAME" | tr '-' '_')
FEATURE_DIR="src/main/java/me/geohod/geohodbackend/feature/$FEATURE_DIR_LOWER"

if [ ! -d "$FEATURE_DIR" ]; then
    print_info "Creating feature directory structure..."
    mkdir -p "$FEATURE_DIR"/{api,service,impl,data,model}
    print_success "Created feature directory structure: $FEATURE_DIR"
else
    print_warning "Feature directory already exists: $FEATURE_DIR"
fi

# Create initial commit
if git diff --quiet && git diff --staged --quiet; then
    print_info "No changes to commit."
else
    print_info "Committing initial feature setup..."
    git add .
    git commit -m "feat: initialize $FEATURE_NAME feature

- Create feature branch: $FEATURE_BRANCH
- Add feature specification: $SPEC_PATH
- Set up initial directory structure

$DESCRIPTION"
    print_success "Committed initial feature setup"
fi

# Display next steps
print_success "Feature initialization completed!"
echo
echo "Next steps:"
echo "1. 📝 Update the feature specification in $SPEC_PATH"
echo "2. 🔧 Implement the feature components"
echo "3. 🧪 Add tests for the new functionality"
echo "4. 📚 Update API documentation"
echo "5. 🔄 Create pull request when ready"
echo
echo "Useful commands:"
echo "• View current branch: git branch --show-current"
echo "• View feature status: git status"
echo "• Push feature branch: git push origin $FEATURE_BRANCH"
echo "• Create pull request: gh pr create --title 'feat: $DESCRIPTION' --body 'Closes #<issue-number>'"
echo
print_info "Happy coding! 🚀"