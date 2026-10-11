# Student Management System

This repository contains two separate versions of the student management app:

- The browser-based version is published with GitHub Pages. Its records are stored in the current browser.
- The JavaFX desktop app is in [`student_managment_system/`](./student_managment_system). It stores records locally on the computer and can be downloaded from the GitHub Releases page.

The browser app and desktop app do not share student records.

## Run the desktop app from source

Install JDK 25 and Maven, then run these commands from the repository root:

```powershell
mvn -f student_managment_system/pom.xml clean verify
mvn -f student_managment_system/pom.xml javafx:run
```

## Desktop downloads

Create and push a version tag to build self-contained desktop packages for Windows, Linux, and macOS:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

The release workflow runs the tests, packages the app with its Java runtime, and attaches the platform-specific downloads to the GitHub Release. The Pages site links to the latest release.

## GitHub Pages

The Pages workflow publishes the static site from `site/` when that directory changes on `main`. Configure the repository's Pages source as **GitHub Actions**.
