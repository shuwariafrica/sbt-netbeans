param (
    [string]$version,
    [switch]$help
)

# Function to display help message
function Show-Help {
    Write-Output "Usage: .\tag-release.ps1 <version>"
    Write-Output ""
    Write-Output "Tags a git release with the specified version."
    Write-Output "The version must match the semantic versioning spec (e.g., 1.0.0, 1.0.0-alpha, 1.0.0+build123)."
    Write-Output "Ensures that the working directory is clean (no uncommitted changes) before tagging."
    Write-Output ""
    Write-Output "Options:"
    Write-Output "  -help    Show this help message and exit"
}

# Check if help flag is provided
if ($help) {
    Show-Help
    exit 0
}

# Check if a version parameter is provided
if (-not $version) {
    Write-Error "Error: Version parameter is required."
    Show-Help
    exit 1
}

# Validate the version parameter against the semantic versioning spec
if ($version -notmatch '^[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?(\+[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?$') {
    Write-Error "Error: Version must match the semantic versioning spec (e.g., 1.0.0, 1.0.0-alpha, 1.0.0+build123)."
    exit 1
}

# Check if the working tree is clean
$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Error "Error: There are uncommitted changes in the working directory."
    exit 1
}

# Compute the tag name
$tagName = "v$version"

# Create an annotated and GPG-signed tag
try {
    git tag --sign --annotate $tagName -m "Release version $tagName"
    Write-Output "Success: Tagged release $tagName."
} catch {
    Write-Error "Error: Failed to create tag."
    exit 1
}
