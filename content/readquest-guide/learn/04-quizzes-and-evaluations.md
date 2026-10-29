# Taking Assessments & Reviewing Mistakes

Active recall is the single most effective technique for mastering technical subjects. ReadQuest pairs theoretical reading with timed, multi-choice assessments that test practical comprehension.

---

## Exploring the Quiz Tab

In the **Quiz** bottom tab, quizzes are organized topic-wise:

```text
+-------------------------------------------------------------+
| Topic-wise Quiz                                             |
+-------------------------------------------------------------+
| [v] Navigation & Reading Fundamentals                       |
|     5 Questions  •  5 Minutes  •  70% to Pass               |
|     [ Start Assessment ]                                    |
|                                                             |
| [>] Customization, Sync & Authoring                         |
|     5 Questions  •  5 Minutes  •  70% to Pass               |
+-------------------------------------------------------------+
```

Each accordion card displays:
- **Question Count:** The exact number of questions in the assessment.
- **Time Limit:** Allocated time in minutes.
- **Pass Threshold:** Minimum passing score required to record mastery (e.g., 70%).

---

## The Quiz Runner Screen

When you launch an assessment, you enter the focused **Quiz Runner**:

1. **Countdown Timer:** Displayed in the header. If you need a pause, tap the pause icon to freeze the timer.
2. **Navigation Bubbles:** A scrollable row of numeric badges (e.g. `1`, `2`, `3`, `4`, `5`) shows your current position and allows quick jumping between questions.
3. **Question Prompt:** Formatted with bold highlights and code snippets when applicable.
4. **Four Options (A, B, C, D):** Select your chosen answer using the radio buttons.
5. **Back Protection:** Pressing the device back button opens an exit confirmation dialog to prevent accidental data loss.

---

## Submission & Results Review

Tapping **Submit Quiz** completes the test and opens the **Quiz Result Review** screen:

### Performance Banner
- **PASSED / FAILED:** Evaluates your percentage against the required passing score.
- **Score Metrics:** Shows total correct answers, percentage score, and time taken.
- **Share Button:** Allows exporting your score to colleagues or social media.

### Question-by-Question Breakdown
Every question presents an in-depth post-mortem:
- **Your Choice:** Highlights your selected option (green if correct, red if incorrect).
- **Correct Answer:** Clearly marks the correct response.
- **Explanation Block:** Provides the rationale explaining *why* the right answer is correct and dissecting common traps.

```text
+-------------------------------------------------------------+
| Question 2                                     [ ✓ CORRECT ]|
| What is the question separator in ReadQuest quiz files?     |
|                                                             |
| (•) \n===\n                                                 |
| ( ) ---                                                     |
|                                                             |
| Explanation:                                                |
| \n===\n is the strict delimiter recognized by the Kotlin    |
| parser to split questions without colliding with markdown   |
| horizontal rules.                                           |
+-------------------------------------------------------------+
```
