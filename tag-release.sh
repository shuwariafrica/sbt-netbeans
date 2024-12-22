#!/bin/bash

# Function to display help message
show_help() {
  echo "Usage: $0 <version>"
  echo
  echo "Tags a git release with the specified version."
  echo "The version must match the semantic versioning spec (e.g., 1.0.0, 1.0.0-alpha, 1.0.0+build123)."
  echo "Ensures that the working directory is clean (no uncommitted changes) before tagging."
  echo
  echo "Options:"
  echo "  -h, --help    Show this help message and exit"
}

# Check if help flag is provided
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
  show_help
  exit 0
fi

# Check if a version parameter is provided
if [ -z "$1" ]; then
  echo "Error: Version parameter is required."
  show_help
  exit 1
fi

# Validate the version parameter against the semantic versioning spec
if ! [[ "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?(\+[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?$ ]]; then
  echo "Error: Version must match the semantic versioning spec (e.g., 1.0.0, 1.0.0-alpha, 1.0.0+build123)."
  exit 1
fi

# Check if the working tree is clean
if [ -n "$(git status --porcelain)" ]; then
  echo "Error: There are uncommitted changes in the working directory."
  exit 1
fi

# Compute the tag name
TAG_NAME="v$1"

# Create an annotated and GPG-signed tag
if git tag --sign --annotate "$TAG_NAME" -m "Release version $TAG_NAME"; then
  echo "Success: Tagged release $TAG_NAME."
else
  echo "Error: Failed to create tag."
  exit 1
fi
