## Q1
Which of the following is NOT one of the three primary bottom navigation tabs in ReadQuest?

### Option A
Learn

### Option B
Extra

### Option C
Quiz

### Option D [CORRECT]
Bookmarks

### Explanation
The three primary tabs in ReadQuest are Learn (syllabus and learning paths), Extra (use cases, curiosities, and history), and Quiz (topic-wise assessments). Settings is reached via the top app bar.

===

## Q2
Where can you switch your active study subject in ReadQuest?

### Option A [CORRECT]
By tapping the Subject Header at the top of the Learn screen

### Option B
By long-pressing the bottom navigation bar

### Option C
Inside the Android system notifications shade

### Option D
By rebooting the Android device

### Explanation
Tapping the Subject Header banner on the Learn screen opens the SubjectPickerBottomSheet, which lets you switch between all locally installed study courses.

===

## Q3
What happens when you tap the copy icon on a code block in the Content Reader?

### Option A
It navigates back to the home screen

### Option B [CORRECT]
It copies the entire code block to your device clipboard

### Option C
It reports a syntax error to GitHub

### Option D
It changes the reader theme to dark mode

### Explanation
Every code block features a copy icon in its top-right corner. Tapping it writes the raw code snippet directly to the Android ClipboardManager.

===

## Q4
Which eye comfort theme preset is specifically optimized for AMOLED battery saving and maximum dark contrast?

### Option A
Sepia

### Option B
Solarized Light

### Option C [CORRECT]
Pure OLED Black

### Option D
Nord Light

### Explanation
The Pure OLED Black preset uses true `#000000` background hex values, allowing AMOLED/OLED displays to physically turn off individual pixels for zero light emission and maximum battery savings.

===

## Q5
How does ReadQuest protect your assessment progress if you accidentally press the back button during a quiz?

### Option A
It automatically submits the quiz with a passing score

### Option B [CORRECT]
It intercepts the back event and displays an exit confirmation dialog

### Option C
It minimizes the app to the background without warning

### Option D
It deletes the quiz file from your device

### Explanation
The QuizRunner screen implements a custom BackHandler that intercepts back presses and displays an AppConfirmDialog, preventing unintended forfeiture of an in-progress quiz.
