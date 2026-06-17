# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/2.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-06-17

Major update to modern Android 16 with a few bug fixes.

### Changed

- Update to Androit 16 (36.1)

### Added

- About: link app title to GitHub repo
- About: display app version

### Fixed

- Scraper uses URL instead of title to resolve recipe title. Fixes bug where
  scraped titles that had been truncated by the Apache directory listing would
  not be added to the app database.
- Spinner selection color in the action bar now shown as white.
