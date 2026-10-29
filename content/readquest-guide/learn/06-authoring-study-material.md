# Authoring Courses, Quizzes & Manifests

ReadQuest is not just a consumer app; it is an open authoring platform. Anyone can fork or create a GitHub repository to build customized courses for software engineering, certification exams, or internal engineering on-boarding.

---

## The Content Folder Structure

Every repository consumed by ReadQuest must conform strictly to the following directory hierarchy:

```text
content/
├── catalog.json                             <-- Root catalog listing courses
└── <subject-id>/                             <-- Kebab-case directory
    ├── subject.json                          <-- Course manifest & structure
    ├── assets/                              <-- Diagram images (.webp, .svg, .png)
    ├── learn/                               <-- Core lesson files (.md)
    ├── extra/                               <-- Real-world use cases & trivia (.md)
    └── quiz/                                <-- Assessment files (.md)
```

---

## Crucial Schema Rules

When creating `subject.json` and Markdown files, obey these contracts:

### 1. No `readingTimeMinutes`
Never define a `readingTimeMinutes` field anywhere in your JSON files. The Kotlin serialization model does not recognize it.

### 2. Extras Gating & Types
- **Types:** Must be one of `"USE_CASE"`, `"CURIOSITY"`, `"HISTORY"`, or `"EXTERNAL_LINK"`.
- **Tiers:** Use `"FREE"` for immediately accessible items, or `"CORE"` with `"unlockAfterPathId": "<path-id>"` to lock the item until the user completes the designated learning path.

---

## Strict Quiz Syntax Contract

Quizzes are split by a deterministic regular expression. If you deviate from the syntax below, questions will fail to parse:

```markdown
## Q1
What is the primary language used to build ReadQuest?

### Option A
Flutter

### Option B [CORRECT]
Kotlin & Jetpack Compose

### Option C
React Native

### Option D
Objective-C

### Explanation
ReadQuest is written 100% in native Kotlin using Jetpack Compose for declarative UI.

===

## Q2
...
```

### The 5 Non-Negotiable Quiz Rules:
1. **Separator:** Use standalone `===` lines surrounded by newlines (`\n===\n`). Never use `---`.
2. **Sequential Headers:** `## Q1`, `## Q2`, `## Q3`, etc.
3. **Four Options:** Exactly 4 options (`### Option A` through `### Option D`).
4. **Correct Marker:** Append ` [CORRECT]` to the header of the single correct option.
5. **Explanation Block:** End each question with an `### Explanation` header followed by rationale text.

---

## Pre-Commit Verification Checklist

Before publishing changes to your GitHub repository:
- [ ] Ensure all IDs use `kebab-case`.
- [ ] Verify `quizzes[].questionCount` matches the exact number of `## Q<N>` blocks.
- [ ] Confirm all images in `.md` exist in `assets/` and are listed in `subject.json -> assets[]`.
- [ ] Increment `"version"` in `subject.json` and `content/catalog.json`.
