# AI Interview Simulator (Java + Swing)

A desktop interview-practice app. Pure Java (JDK 11 or newer), no external libraries.

## 1. How to run

Open a terminal **inside the project folder** (the folder that contains `src` and `data`):

```
javac -d out -sourcepath src src/Main.java
java -cp out Main
```

Or just double-click / run `run.bat` (Windows) or `./run.sh` (Mac/Linux).
Always start from the project folder, because `data/questions.txt` and `data/interview_history.txt` are created there.

IDE (IntelliJ / Eclipse / VS Code): create a Java project, copy the `src` folder into it, run `Main.java`.
Set the working directory to the project root so the `data` folder is created next to `src`.

## 2. Architecture

```
src/
|-- Main.java                  starts the app
|-- model/                     DATA classes
|   |-- Person.java            abstract base class
|   |-- Candidate.java         extends Person
|   |-- Role.java, Difficulty.java   enums
|   |-- Question.java          one question + keyword groups
|   |-- Answer.java            answer + its scores + feedback
|   |-- Reportable.java        interface
|   `-- InterviewResult.java   final report (implements Reportable)
|-- service/                   LOGIC classes
|   |-- Evaluator.java         interface
|   |-- EvaluationService.java rule-based scoring (implements Evaluator)
|   |-- QuestionService.java   loads questions, picks random ones
|   |-- InterviewService.java  interview flow, adaptive difficulty, report
|   `-- FileService.java       reads questions.txt, saves/reads history
|-- gui/                       SCREENS (Swing)
|   |-- WelcomeFrame.java      enter name
|   |-- DashboardFrame.java    stats + actions
|   |-- RoleSelectionFrame.java role + difficulty cards
|   |-- InterviewFrame.java    questions, timer, answer box, feedback
|   |-- ResultFrame.java       final report
|   `-- HistoryDialog.java     previous interviews
`-- utils/
    |-- QuestionBank.java      built-in questions (written to data/questions.txt)
    |-- TimerManager.java      countdown timer on its own thread
    `-- UI.java                colours, rounded buttons and cards
data/
|-- questions.txt              question bank (you can edit / add questions)
`-- interview_history.txt      created after the first completed interview
```

Flow: `Welcome -> Dashboard -> RoleSelection -> Interview (5 questions) -> Result -> Dashboard`

## 3. What each class does

| Class | Job |
|---|---|
| Person | Abstract class with `name` and abstract `getDescription()` |
| Candidate | The person taking the interview (name, role, starting difficulty) |
| Role / Difficulty | Enums. Difficulty holds time limit (60/90/120 s) and `harder()` / `easier()` |
| Question | Text, role, difficulty, category and keyword groups. `fromLine()` parses a line of questions.txt |
| Answer | Candidate's text, 4 sub-scores, final score, feedback, matched/missing concepts |
| InterviewResult | Averages, strengths, weaknesses, recommendation. `toReportText()` builds the saved report |
| Evaluator | Interface: `evaluate(question, text, seconds, timedOut)` |
| EvaluationService | The rule-based "AI" (see section 5) |
| QuestionService | Groups questions in a `HashMap` by role+difficulty; `Random` picks an unused one |
| InterviewService | Keeps answers, applies adaptive difficulty, builds `InterviewResult` |
| FileService | All file handling with `java.nio.file` and `try/catch` |
| TimerManager | Counts down on a separate `Thread` and updates the GUI with `SwingUtilities.invokeLater` |
| UI | Custom painted `RoundedPanel`, `RoundedButton`, `ChoiceButton` so it does not look like default Swing |

## 4. Question file format (data/questions.txt)

```
ROLE|DIFFICULTY|CATEGORY|QUESTION|keyword1,keyword2/synonym,keyword3
JAVA_DEVELOPER|INTERMEDIATE|OOP|Explain inheritance in Java.|parent/superclass,child/subclass,extends,reuse,override
```
`,` separates concepts and `/` separates synonyms of one concept. Roles: JAVA_DEVELOPER, WEB_DEVELOPER, SOFTWARE_ENGINEER, DATA_ANALYST, GENERAL_HR. Difficulty: BEGINNER, INTERMEDIATE, ADVANCED. 45 questions are included (3 per role per level).

## 5. How answers are scored

1. Lower-case the answer and remove punctuation.
2. For every concept, check its keyword or any synonym. Matching is by **word start** (`extend` matches `extends`) and tolerates **one typo** in words of 6+ letters.
3. `coverage = matched concepts / total concepts`.
4. Four scores (0-10):
   - **Technical** = coverage / 0.7 (+0.5 if an example is given)
   - **Accuracy** = coverage / 0.6, minus penalties for "I don't know / maybe" and for keyword-stuffing
   - **Completeness** = 65% coverage + 35% answer length compared with the ideal length of that level
   - **Communication** = length, number of sentences, connectors (because, for example...), word variety, capital letter and full stop
5. `Final = 0.30*Technical + 0.20*Communication + 0.25*Accuracy + 0.25*Completeness` (weights are constants at the top of `EvaluationService`; change them freely).

Adaptive difficulty: score >= 8 -> next question is harder; score <= 4 -> easier; otherwise same level.
Timer: when it reaches 00:00 the answer is auto-submitted, evaluated, and after 3 seconds the next question opens.
Strong answer = score >= 7. In the report, *Average Score* is the mean of the four sub-scores and *Overall Score* is the weighted average.

## 6. Sample questions

- HR: Tell me about yourself. / What are your strengths? / Why should we hire you?
- Java: What is OOP? / Explain inheritance in Java. / Difference between ArrayList and LinkedList. / What is exception handling in Java? / Explain multithreading in Java.
- Web: What is HTTP? / Difference between frontend and backend. / What is a REST API?
- Data Analyst: Explain SQL joins. / What is the difference between WHERE and HAVING?
- Software Engineer: Explain Big-O notation. / Process vs thread.

## 7. Sample output (saved in data/interview_history.txt)

```
======================================================
 AI INTERVIEW REPORT
======================================================
Date               : 08 Oct 2026, 16:20
Candidate          : Rahul
Role               : Java Developer
Starting Difficulty: Beginner
Questions Attempted: 5
Strong Answers     : 3
Average Score      : 6.8/10
Technical Score    : 7.2/10
Communication Score: 6.4/10
Accuracy Score     : 7.5/10
Completeness Score : 6.1/10
Overall Score      : 6.9/10
Duration           : 06:42

Strengths:
  - Good understanding of OOP
  - Strong technical knowledge for the Java Developer role

Weaknesses:
  - Exception Handling needs improvement
  - Answers could be more detailed

Recommendation:
  Good candidate for an entry-level Java Developer interview. Revise the weaker topics once more.
======================================================
```
(The numbers above are an example; every run gives its own values.)

On-screen feedback example: `Score: 8.5/10 - Excellent answer. You covered: parent, child, class, reuse. You could improve your answer by mentioning: extends.`

## 8. OOP concepts used

- **Classes and objects**: `Question`, `Answer`, `Candidate`, `InterviewResult` are created with `new`.
- **Encapsulation**: all fields are `private`; access through getters/setters.
- **Inheritance**: `Candidate extends Person`; every frame `extends JFrame`; `RoundedButton extends JButton`.
- **Polymorphism**: `Evaluator evaluator = new EvaluationService()` in `InterviewService`; `getDescription()` and `toReportText()` are overridden methods.
- **Abstraction**: `Person` is abstract; `Evaluator` hides *how* scoring is done.
- **Interfaces**: `Evaluator`, `Reportable`, `TimerManager.TimerListener`.
- Also used: `ArrayList`, `HashMap`, `HashSet`, `TreeMap`, `Random`, exception handling (try/catch for `IOException`, `IllegalArgumentException`), file handling (`Files`), Date/Time API (`LocalDateTime`, `Instant`, `Duration`), multithreading (timer thread), Java Swing.

## 9. Viva questions and answers

1. **What is the aim of the project?** To let a user practise interviews: ask role-based questions, score the answers with rules, give feedback and produce a report.
2. **How does evaluation work without a real AI?** It checks the answer for important concepts (keywords + synonyms), then scores technical, accuracy, completeness and communication, and combines them with weights.
3. **How is it better than simple keyword counting?** It uses synonyms, prefix matching, typo tolerance, answer length, sentence structure and penalties for uncertain wording or keyword stuffing.
4. **Where did you use inheritance?** `Candidate extends Person`, and all frames extend `JFrame`.
5. **Where did you use polymorphism?** `InterviewService` holds an `Evaluator` reference; at runtime it is an `EvaluationService`. Another evaluator (for example an API based one) can replace it without changing other code.
6. **Why an interface for Evaluator?** To follow "program to an interface": the scoring method can change without touching the interview logic.
7. **Why is TimerManager on a separate thread?** If the countdown ran on the Swing thread, the window would freeze. The thread sleeps one second at a time and calls `SwingUtilities.invokeLater` to update the label safely.
8. **What is `SwingUtilities.invokeLater`?** Swing is not thread-safe; GUI changes must happen on the Event Dispatch Thread. This method schedules code there.
9. **How does adaptive difficulty work?** After each answer, `InterviewService.adaptDifficulty()` moves the level up for score >= 8 and down for score <= 4 using `Difficulty.harder()` / `easier()`.
10. **Why `HashMap` in QuestionService?** To store questions by key `ROLE_DIFFICULTY` and fetch the right list quickly.
11. **How is data saved?** `FileService` appends the report text to `data/interview_history.txt` with `Files.write(..., APPEND)`. The dashboard reads the "Overall Score" lines back for statistics.
12. **How do you handle errors?** `IOException` is caught in `FileService`; invalid question lines throw `IllegalArgumentException` and are skipped with a message.
13. **How can you add new questions?** Add a line to `data/questions.txt` in the same format. No code change is needed.
14. **How can you extend the project?** Add voice input, more roles, a real AI API evaluator (new class implementing `Evaluator`), charts for history, or a database instead of text files.
