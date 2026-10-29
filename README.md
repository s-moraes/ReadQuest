# ReadQuest

<div align="center">

![ReadQuest Logo](app/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp)

**An offline-first, high-performance technical study hub and knowledge engine built natively in Jetpack Compose.**

[![Android](https://img.shields.io/badge/Platform-Android_13+_(API_33+)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![CI](https://github.com/s-moraes/ReadQuest/actions/workflows/android-ci.yml/badge.svg)](https://github.com/s-moraes/ReadQuest/actions)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/s-moraes/ReadQuest/pulls)

</div>

---

## 📖 Table of Contents

- [Overview & Philosophy](#-overview--philosophy)
- [Key Features](#-key-features)
  - [1. Structured Syllabus (Learn Tab)](#1-structured-syllabus-learn-tab)
  - [2. Deep Dives & Use Cases (Extra Tab)](#2-deep-dives--use-cases-extra-tab)
  - [3. Active Recall Assessments (Quiz Tab)](#3-active-recall-assessments-quiz-tab)
  - [4. Distraction-Free Content Reader](#4-distraction-free-content-reader)
  - [5. Git-Powered Decentralized Sync](#5-git-powered-decentralized-sync)
- [Pre-Bundled Courses](#-pre-bundled-courses)
- [Repository & Monorepo Structure](#-repository--monorepo-structure)
- [Authoring Custom Courses](#-authoring-custom-courses)
- [Tech Stack & Architecture](#-tech-stack--architecture)
- [Getting Started & Build Instructions](#-getting-started--build-instructions)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Overview & Philosophy

Most mobile documentation and flashcard apps either rely on sluggish `WebView` wrappers or restrict content to superficial bullet points. **ReadQuest** was built from scratch to combine in-depth engineering texts with active recall testing, using three guiding principles:

- **100% Native Jetpack Compose:** Zero WebViews. Raw Markdown is converted directly into native UI primitives, delivering rock-solid 60/120 fps scrolling, cold start times under 35ms, and a lightweight memory footprint (< 18MB).
- **Context Before Recall:** Deep conceptual lessons come first, supplemented by real-world architecture case studies, and cemented through timed, active recall assessments.
- **Git-Powered Decentralization:** Anyone can publish courses using a standard GitHub repository. The app consumes raw markdown manifests without requiring custom backend servers or subscriptions.

---

## ✨ Key Features

### 1. Structured Syllabus (Learn Tab)
- **Active Subject Banner:** Displays course progress, module breakdown, and an intuitive bottom sheet course switcher.
- **Continue Learning Hero Card:** Automatically surfaces the earliest unread topic in your syllabus so you can jump straight back into studying with one tap.
- **Modular Learning Paths:** Accordion navigation grouping topics logically (e.g., *Foundations*, *Advanced Patterns*).
- **Topic Completion:** Mark topics as completed, track granular progress, and unlock prerequisite advanced material.

### 2. Deep Dives & Use Cases (Extra Tab)
- **Categorized Content:** Filter between `Use Cases`, `Curiosities`, `History`, and `External Links`.
- **Prerequisite Gating (Free vs. Core Tier):**
  - `FREE`: Open for reading immediately.
  - `CORE`: Locked behind prerequisite learning paths to ensure you master fundamentals before jumping into edge cases or system designs.

### 3. Active Recall Assessments (Quiz Tab)
- **Timed Simulation:** Clean countdown timer with pause/resume capabilities and back-button exit protection to prevent accidental cancellation.
- **Bubble Navigation:** Horizontal question carousel allowing non-linear navigation between questions.
- **Post-Assessment Review:** Immediate pass/fail verdict, scoring percentage, and a question-by-question post-mortem with detailed theoretical explanations for both correct answers and distractors.

### 4. Distraction-Free Content Reader
- **Scroll Progress Indicator:** Real-time progress bar pinned beneath the top app bar.
- **Native Syntax Highlighting:** Built-in lexer for `kotlin`, `java`, `json`, `sql`, `bash`, and more, featuring an animated one-tap copy button.
- **Text Selection & Sharing:** Floating native action bar with instant text selection and system share integration.
- **Eye Comfort Presets:**
  - 🖤 **True OLED Black** (`#000000`) for zero battery drain and night reading.
  - 📜 **Warm Sepia** (`#F4ECD8`) for book-like daytime comfort.
  - 🌌 **Solarized Light & Dark** to reduce optical fatigue.
  - 🎨 **Custom Hex Mode** for personalized backgrounds and text colors.
- **Typography Scaling:** Smooth font size slider (from `0.8x` to `1.4x`).

### 5. Git-Powered Decentralized Sync
- **Zero Backend Architecture:** Connects directly to GitHub via `raw.githubusercontent.com`.
- **Intelligent Branch Resolution:** Automatically detects `main` or `master` branches, handles tree URLs, and falls back gracefully.
- **Atomic Storage (`AtomicJsonStore`):** State and progress updates are written atomically, guaranteeing zero corrupted files if the app process terminates abruptly or your device battery dies.
- **100% Offline Availability:** All lessons, manifests, and diagrams in `assets/` are cached locally for offline study on flights, commutes, or remote areas.

---

## 📚 Pre-Bundled Courses

ReadQuest comes with 5 comprehensive engineering courses:

| Course | Topics | Description |
| :--- | :---: | :--- |
| **Mastering ReadQuest** | 6 topics + 2 quizzes | Full user guide to navigation, themes, quizzes, sync, and custom course authoring. |
| **System Design & Architecture** | 8 topics + 4 quizzes | Distributed systems, scalability, load balancing, caching, replication, sharding, and consensus. |
| **Prep4Java: Foundations** | 6 topics + 6 quizzes | Core Java syntax, variables, operators, loops, arrays, memory models, and certification drills. |
| **Database Migrations with Flyway** | 7 topics + 2 quizzes | Schema evolution, versioned migrations, repeatable scripts, CI/CD integration, and zero-downtime practices. |
| **Cloud-Native Concurrency & State** | 6 topics + 3 quizzes | Microservices state, distributed locks, actor systems, big data parallelism, and reactive pipelines. |

---

## 📂 Repository & Monorepo Structure

This repository is dual-purposed: it contains both the **native Android application** and the **central course catalog** that the app synchronizes from GitHub:

```text
ReadQuest/
├── .github/
│   └── workflows/
│       └── android-ci.yml             # GitHub Actions CI (build & unit tests)
├── app/                               # Native Android Studio Project
│   ├── app/
│   │   ├── src/main/java/...          # Jetpack Compose UI, ViewModels, Repositories
│   │   ├── src/test/java/...          # Comprehensive unit test suite (98+ tests)
│   │   └── build.gradle.kts           # App module configuration
│   ├── gradle/libs.versions.toml      # Version catalog (Compose BOM, Kotlin, AGP)
│   ├── build.gradle.kts               # Top-level Gradle script
│   └── gradlew                        # Gradle wrapper executable
├── content/                           # Educational Content Catalog (consumed via Git)
│   ├── catalog.json                   # Global index of all courses
│   ├── HOW_TO_USE_READQUEST.md        # Comprehensive app manual
│   ├── PROMPT_SYSTEM_CONTENT_GEN.md   # AI authoring & schema compliance specification
│   └── readquest-guide/               # Course: Mastering ReadQuest
├── .gitignore                         # Android & IDE ignore rules
├── LICENSE                            # MIT License
└── README.md                          # Project documentation
```

---

## 🛠️ Authoring Custom Courses

Anyone can create and host courses for ReadQuest using their own GitHub repository!

### Directory Contract:
```text
content/
├── catalog.json
└── <subject-id>/
    ├── subject.json
    ├── assets/
    ├── learn/
    ├── extra/
    └── quiz/
```

1. **Manifest (`subject.json`):** Defines syllabus hierarchy, learning paths, quizzes, and prerequisites.
2. **Quiz Format (`quiz/*.md`):** Uses deterministic parsing (`## Q1`, `### Option A`..`### Option D`, `[CORRECT]`, and `\n===\n` delimiters).
3. **Connect in ReadQuest:** Open **Settings** > enter your repository URL (e.g. `https://github.com/<your-username>/<your-repo>`) > tap **Sync Content from Web**.

> 💡 For detailed specifications and validation rules, see [HOW_TO_USE_READQUEST.md](content/HOW_TO_USE_READQUEST.md) and [PROMPT_SYSTEM_CONTENT_GEN.md](content/PROMPT_SYSTEM_CONTENT_GEN.md).

---

## 🧰 Tech Stack & Architecture

- **Language:** Kotlin 2.2.10
- **UI Toolkit:** Jetpack Compose (Material 3, BOM `2026.02.01`)
- **Architecture:** Unidirectional Data Flow (Clean MVVM / MVI pattern)
  - `StudyHubViewModel` + `StateFlow`
  - `StudyUiStateMapper` for presentation models
- **Serialization:** `kotlinx.serialization` (JSON parsing)
- **Local Storage:** `AtomicJsonStore` (atomic temporary file rename strategy)
- **Network & Parsing:** Coroutines Dispatchers.IO, custom `GitHubUrlResolver`, regex-based markdown parser
- **Testing:** JUnit 4, AndroidX Test, Espresso

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
- **Android Studio:** Ladybug (2024.2+) or Meerkat (2024.3+)
- **JDK:** Java 17 or Java 21
- **Android SDK:** API 33+ (compileSdk 37)

### Building from Source

1. **Clone the repository:**
   ```bash
   git clone https://github.com/s-moraes/ReadQuest.git
   cd ReadQuest
   ```

2. **Open the project:**
   Open the `ReadQuest/app` folder in Android Studio.

3. **Run Unit Tests:**
   ```bash
   cd app
   ./gradlew testDebugUnitTest
   ```

4. **Build Debug APK:**
   ```bash
   cd app
   ./gradlew assembleDebug
   ```
   The generated APK will be located at:
   `app/app/build/outputs/apk/debug/app-debug.apk`

---

## 🤝 Contributing

Contributions are warmly welcome! Whether you are:
- Adding a new engineering course or topic to `content/`
- Improving markdown rendering or syntax highlighting
- Adding new reader color palettes or accessibility improvements

Please see [PROMPT_SYSTEM_CONTENT_GEN.md](content/PROMPT_SYSTEM_CONTENT_GEN.md) before modifying content files to ensure compliance with the parser schema.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'feat: add AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See [LICENSE](LICENSE) for more information.

---

<div align="center">
Developed by <b>Saulo A. Moraes</b> · <a href="https://github.com/s-moraes">@s-moraes</a>
</div>
