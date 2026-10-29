# Distraction-Free Reading & Gesture Controls

The **Content Reader** screen is engineered for sustained focus, readability, and immediate practical engagement. It parses native Markdown directly into Android UI primitives without WebViews.

---

## Reader Anatomy

When you open any topic or extra article, the reader presents a clean reading environment:

- **Scroll Progress Indicator:** A thin bar pinned below the top toolbar tracks your exact position in the article in real time.
- **Top Bar Actions:** Provides an instant return arrow, the current topic title, and the **Appearance (Aa)** button.
- **Dual-Action Bottom Bar:** Houses progress triggers and next-step navigations.

```text
+-------------------------------------------------------------+
| [<] Topic Title                                        [Aa] |
| [======================== 65% Progress ===================] |
+-------------------------------------------------------------+
|                                                             |
|  # Markdown Content                                         |
|  Rendered natively with syntax highlighting & tables.       |
|                                                             |
|  ```kotlin                                                  |
|  val readQuest = ReadQuest() [Copy]                         |
|  ```                                                        |
|                                                             |
+-------------------------------------------------------------+
| [✓ Mark as Completed]                  [Next Topic ->]      |
+-------------------------------------------------------------+
```

---

## Code Blocks & Native Toolbars

Technical study often requires inspecting code samples. ReadQuest elevates reading code on mobile screens:

### 1. High-Precision Syntax Highlighting
Code snippets automatically highlight keywords, annotations, literals, strings, and comments across languages including `kotlin`, `java`, `json`, `sql`, and `bash`.

### 2. One-Tap Clipboard Copy
Every code block features a copy icon in its top-right corner. Tapping it copies the entire snippet directly to your device clipboard with an animated confirmation checkmark.

### 3. Shareable Selection Toolbar
Selecting any portion of the text invokes a specialized floating toolbar that lets you copy or share text snippets directly to messaging apps or note-taking tools.

---

## Progress Progression & Related Material

At the conclusion of each lesson, ReadQuest suggests your next logical step:
- **Mark as Completed:** Saves your reading progress to local storage.
- **Related Quizzes:** If a topic has an associated quiz (`relatedQuizId`), a direct shortcut button launches the quiz immediately.
- **Related Extras:** Deep-dive cards (`relatedExtraIds`) connect theoretical lessons to real-world architectures and curiosities.
- **Next Topic Navigation:** Seamlessly jump to the next chapter in your learning path.
