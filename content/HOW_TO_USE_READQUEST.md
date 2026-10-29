# ReadQuest: Comprehensive User & Content Authoring Guide

Welcome to the definitive guide for **ReadQuest** (`com.saulo.readquest`), an offline-first, high-performance technical study hub built natively in **Jetpack Compose**. 

Whether you are an end-user studying engineering topics on the go, or an educator/team lead authoring custom documentation, this manual covers every screen, control, configuration, and content specification.

---

## Table of Contents
1. [App Overview & Philosophy](#1-app-overview--philosophy)
2. [First-Time Launch & Content Sync](#2-first-time-launch--content-sync)
3. [Tour of the User Interface](#3-tour-of-the-user-interface)
   - [The Learn Tab](#the-learn-tab)
   - [The Extra Tab](#the-extra-tab)
   - [The Quiz Tab](#the-quiz-tab)
   - [The Settings Screen](#the-settings-screen)
4. [The Reading Experience](#4-the-reading-experience)
   - [Progress Bar & Navigation](#progress-bar--navigation)
   - [Code Snippets & Copying](#code-snippets--copying)
   - [Text Selection & Sharing](#text-selection--sharing)
   - [Appearance & Eye Comfort Presets](#appearance--eye-comfort-presets)
5. [Taking Assessments & Reviewing Mistakes](#5-taking-assessments--reviewing-mistakes)
6. [Offline Architecture & Atomic Storage](#6-offline-architecture--atomic-storage)
7. [Authoring Custom Courses on GitHub](#7-authoring-custom-courses-on-github)
8. [Troubleshooting & FAQ](#8-troubleshooting--faq)

---

## 1. App Overview & Philosophy

Most mobile documentation and flashcard apps either rely on sluggish `WebView` browsers or restrict content to rigid 5-word cards. ReadQuest is built on three core engineering pillars:

- **100% Native Jetpack Compose:** Zero WebViews. Raw Markdown is converted directly into native UI primitives, delivering rock-solid 60/120 fps scrolling, cold start times under 35ms, and a lightweight memory footprint (< 18MB).
- **Context Before Recall:** Deep conceptual lessons come first, supplemented by real-world architecture case studies, and cemented through timed, active recall assessments.
- **Git-Powered Decentralization:** Anyone can publish courses using a standard GitHub repository. The app consumes raw markdown manifests without requiring custom backend servers or subscriptions.

---

## 2. First-Time Launch & Content Sync

When you launch ReadQuest without pre-bundled content, the app displays the **Empty Content Screen**. To load your study material:

1. Tap **Connect to Repository** (or tap the **Settings** gear icon in the top app bar).
2. Under **Content Sync & Repository**, view or update the **Repository URL**:
   - Default: `https://github.com/s-moraes/ReadQuest` (or your team's custom repository).
3. Tap **Sync Content from Web**.
4. ReadQuest connects to GitHub, auto-detects `main` or `master` branches, downloads `catalog.json`, courses, images, and quizzes, and caches them atomically on your device.
5. Once complete, you can disconnect from Wi-Fi or cellular data; the entire catalog is available **100% offline**.

---

## 3. Tour of the User Interface

### The Learn Tab
The **Learn** tab is your structured home base:
- **Active Subject Banner:** Displays the current course title, icon, and overall completion percentage. Tapping this banner opens the **Subject Picker Bottom Sheet**, allowing you to switch between multiple installed courses.
- **Continue Learning Hero Card:** Automatically surfaces the earliest unread topic in your syllabus so you can jump straight back into studying with one tap.
- **Learning Paths Accordion:** Lists all modular paths (e.g. *Foundations*, *Advanced Patterns*). Each card displays the number of completed topics and overall path progress. Tapping a path opens the **Path Topics Screen**.

### The Extra Tab
The **Extra** tab offers enrichment materials beyond the core syllabus:
- **Filter Chips:** Filter items by category: `All`, `Use Cases`, `Curiosities`, `History`, or `External Links`.
- **Free vs. Core Tier:**
  - `FREE`: Open for reading immediately.
  - `CORE`: Marked with a padlock icon. These items remain locked until you complete all topics within their designated prerequisite learning path.
- **External Links:** Direct links to official documentation (e.g., Jetpack Compose Docs) that launch directly in your system browser.

### The Quiz Tab
The **Quiz** tab hosts topic-wise assessments:
- Each quiz displays its **Question Count**, **Time Limit in Minutes**, and **Pass Percentage**.
- Tap any accordion card to expand it and review its objectives.
- Tap **Start Assessment** to begin.

### The Settings Screen
Accessed via the gear icon in the top right of the top app bar:
- **Sync Settings:** View the last sync timestamp, customize the GitHub repository URL, and trigger manual content synchronization.
- **Theme Selection:** Choose between `System`, `Light`, `Dark`, or activate custom hex color palettes.
- **Typography Scaling:** Adjust font scaling from `0.8x` to `1.4x` via a smooth slider.
- **Progress Reset:** Reset progress for the active subject (clearing topic completions and quiz scores) without losing downloaded content.

---

## 4. The Reading Experience

When opening any lesson in the **Content Reader**, the interface is stripped of distractions:

### Progress Bar & Navigation
- A thin progress indicator pinned underneath the top app bar tracks your scroll position in real time.
- Tapping **[<]** returns to the syllabus.
- Tapping **Aa** opens the **Reader Appearance Sheet**.

### Code Snippets & Copying
- Technical code fences (`kotlin`, `java`, `json`, `sql`, `bash`) feature native syntax highlighting.
- Every code block has a **Copy** button in the upper right. Tapping it copies the snippet to your clipboard and displays an animated checkmark.

### Text Selection & Sharing
- Selecting text in the reader activates a floating native action bar.
- Tap **Copy** to copy highlighted passages, or **Share** to send selections directly to study partners or note-taking apps.

### Appearance & Eye Comfort Presets
ReadQuest includes built-in palettes designed for prolonged reading:
- **Sepia:** Warm paper tones (`#F4ECD8` background, `#2C2416` text) for cozy indoor reading.
- **Solarized Light / Dark:** Low-glare indigo and slate palettes engineered to reduce optical fatigue.
- **Pure OLED Black:** True `#000000` background that turns off AMOLED pixels completely for maximum battery life and zero night glare.
- **Custom HEX Mode:** Enter any background and text HEX colors.

---

## 5. Taking Assessments & Reviewing Mistakes

Assessments in ReadQuest simulate test environments and enforce active recall:

1. **Timed Running Screen:** A countdown timer runs in the top bar. You can pause and resume the timer at any time.
2. **Question Navigation Bubbles:** A horizontal carousel of question numbers lets you jump freely between questions.
3. **Four Options (A–D):** Select your chosen answer using the responsive radio list.
4. **Exit Protection:** Pressing the Android back button triggers a warning modal preventing accidental quiz cancellation.
5. **Detailed Review:** Upon submission, the **Quiz Result Review** screen provides:
   - A clear **PASSED** or **FAILED** banner based on the pass threshold.
   - Percentage score and time spent.
   - A question-by-question post-mortem showing your selected choice, the correct answer, and an in-depth **Explanation** of the underlying theory.

---

## 6. Offline Architecture & Atomic Storage

ReadQuest stores data locally on your Android device:
- **`AtomicJsonStore`:** All user progress and setting states are committed using atomic file writes (writing to a temporary file before renaming). This eliminates the risk of corrupted state files if your device battery dies or the process is stopped.
- **Asset Caching:** Images referenced in `assets/` are fetched during repository synchronization and stored locally, ensuring diagrams display seamlessly offline.

---

## 7. Authoring Custom Courses on GitHub

Anyone can publish a course by pushing files to a GitHub repository following the directory structure:

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

### Essential Manifest Rules:
- **Catalog (`content/catalog.json`):** Registers courses and paths:
  ```json
  {
    "version": 1,
    "subjects": [
      {
        "id": "my-course",
        "title": "My Custom Course",
        "description": "Course summary...",
        "version": 1,
        "courseJsonPath": "content/my-course/subject.json"
      }
    ]
  }
  ```
- **Manifest (`content/<subject-id>/subject.json`):**
  - All IDs must be `kebab-case`.
  - Do **NOT** include `readingTimeMinutes`.
  - `quizzes[].questionCount` must match the exact number of `## Q<N>` blocks in the corresponding `.md` file.

### Strict Quiz File Format (`quiz/*.md`):
1. Must begin immediately with `## Q1` (no frontmatter, no H1 title).
2. Separate questions using `\n===\n` (never use `---`).
3. Exactly four options: `### Option A` through `### Option D`.
4. Append ` [CORRECT]` to the single correct option header.
5. End every question block with `### Explanation`.

---

## 8. Troubleshooting & FAQ

#### Q: The app displays "Empty Content Screen" after syncing.
**A:** Ensure your GitHub repository contains `content/catalog.json` at the root, and verify that the JSON syntax is valid (no trailing commas or invalid quotes).

#### Q: Why is a CORE extra item showing a padlock icon?
**A:** CORE items require completing their prerequisite learning path. Go to the **Learn** tab, open the required path, read all topics, and tap **Mark as Completed** on each.

#### Q: How do I force the app to re-download fresh updates?
**A:** In your repository, increment the `"version"` integer in both `content/<subject-id>/subject.json` and `content/catalog.json`. Then open ReadQuest **Settings** and tap **Sync Content from Web**.
