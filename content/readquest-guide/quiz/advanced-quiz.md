## Q1
What delimiter must be used on a standalone line between questions in a ReadQuest quiz file?

### Option A
`---`

### Option B [CORRECT]
`===`

### Option C
`***`

### Option D
`### END`

### Explanation
ReadQuest's QuizMarkdownParser uses the regex `\r?\n===\r?\n` to split question blocks. Horizontal rules like `---` are reserved for general markdown lessons and must never be used as quiz question separators.

===

## Q2
Which property must NEVER be included anywhere inside a `subject.json` manifest?

### Option A
`passPercentage`

### Option B
`unlockAfterPathId`

### Option C [CORRECT]
`readingTimeMinutes`

### Option D
`timeLimitMinutes`

### Explanation
Rule 1 of the strict subject.json contract states: never include a `readingTimeMinutes` field anywhere in subject.json, as it violates the Kotlin serialization data transfer model.

===

## Q3
When syncing from a root GitHub URL (`https://github.com/user/repo`), which branches does ReadQuest try to connect to?

### Option A
`dev` and `staging`

### Option B [CORRECT]
`main` and `master`

### Option C
`trunk` and `prod`

### Option D
Only `release`

### Explanation
GitHubUrlResolver generates fallback candidate URLs trying the `main` branch first, followed automatically by the `master` branch if `main` returns a 404.

===

## Q4
What is the effect of setting `"tier": "CORE"` on an extra item with `"unlockAfterPathId": "getting-started"`?

### Option A
The item can only be read if the user pays a subscription fee

### Option B [CORRECT]
The item is locked until the user completes all topics inside the `getting-started` learning path

### Option C
The item is hidden from the search catalog completely

### Option D
The item opens immediately with a warning dialog

### Explanation
CORE tier items provide progressive gamification: they display a padlock icon and remain locked until every topic in the designated `unlockAfterPathId` is marked as completed.

===

## Q5
Why does ReadQuest render Markdown through native Jetpack Compose nodes instead of an Android WebView?

### Option A
Because WebViews cannot display text in dark mode

### Option B
Because Google Play forbids WebViews in educational apps

### Option C [CORRECT]
To achieve native 60fps scrolling, instant startup under 35ms, and low memory usage under 18MB

### Option D
Because Markdown cannot be parsed into HTML

### Explanation
Native Compose parsing eliminates the 90MB+ memory footprint and input lag associated with Chromium WebViews, guaranteeing high frame rates and instantaneous theme transitions.
