# App Anatomy & Core Navigation

ReadQuest is an offline-first technical study companion designed for focused reading, retention, and self-assessment. Unlike conventional readers that rely on heavy web wrappers, ReadQuest is built completely in native **Jetpack Compose**, guaranteeing 60fps scrolling, instant startup times, and minimal memory overhead.

---

## The Three Primary Tabs

The bottom navigation bar provides direct access to the three pillars of your learning journey:

| Tab Name | Purpose & Workflow |
| :--- | :--- |
| **Learn** | Your structured syllabus. Displays current subject progress, resume cards, and learning paths. |
| **Extra** | Supplementary engineering content: real-world case studies, architecture patterns, and curiosities. |
| **Quiz** | Topic-wise timed assessments with active recall evaluation and mistake breakdowns. |

```text
+-------------------------------------------------------------+
| Top Bar: [Course Title]                          [Settings] |
+-------------------------------------------------------------+
|                                                             |
|  [Learn Screen]                                             |
|  - Overall Progress Bar (e.g. 42% Completed)               |
|  - "Continue Learning" Quick Resume Card                    |
|  - Learning Paths & Topics Accordion                        |
|                                                             |
+-------------------------------------------------------------+
| Floating Bottom Bar: [ Learn ]   [ Extra ]   [ Quiz ]       |
+-------------------------------------------------------------+
```

---

## Active Subject Switcher

ReadQuest supports multiple independent study subjects at the same time (e.g., *System Design*, *Kotlin Concurrency*, or *ReadQuest Guide*).

1. Tap the **Subject Header** at the top of the Learn screen.
2. A bottom sheet displays all courses available in your local catalog.
3. Select any subject to instantly switch your syllabus, extras, and quizzes.

> [!NOTE]
> Every subject maintains its own isolated progress record. Switching subjects preserves your exact place, completed topics, and quiz scores.

---

## Navigating Learning Paths

Within the **Learn** tab, topics are grouped into logical **Learning Paths**:
- **Progress Badges:** Each path card shows your completion count (e.g., `3/5 Completed`).
- **Interactive Topics:** Tap any path to open its dedicated topic checklist.
- **Sequential Flow:** The hero card at the top always points to your earliest unread topic, so you can resume studying with a single tap.
