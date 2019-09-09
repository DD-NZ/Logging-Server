#NOTE
BlackBox Test should take around 40s to run.

### related commands

To generate reports for JDepend and Spotbugs run **mvn site**

To run tests and generate Jacoco report run **mvn test**

### server QA
1. **Jacoco gaps**<br> Over all it appears my test coverage has a poor overall performance of only 62%. This is because the BlackBox test is not included in the coverage.<br>

  - LogEvent 100%

  - Log Servlet 96%<br>The remaining 6% is due to a Catch statement which was never entered. The catch is for the LogEvent -> JSON conversion. It should never enter this statement as all LogEvents inside the server should not be flawed, if they were, they would have not been allowed into the server storage on entry.

  - LogStorage	12%<br> This contains the code which converts the logs stored in the server to a CSV file. The testing of this code was done in the BlackBox tests and so none of it was recorded in the coverage.

  - LogStorage.Level 100%<br>

  - LogStorage.new Comparator() 0%<br>Comparitor for ordering dates for CSV file so again was not recorded in coverage

  - RandomLogCreator 100%<br> This was a helper class creating mock logs for the tests

  - StatsServlet	0% <br> not covered due to stats only being tested in black box.

2. **JDepend**<br> My Server package has distance of 0. It aligns with Mavens "Ideal Package", it is a pure implementation. It is completely concrete and has an instability of 1.

3. **Spotbugs**<br> No bugs.
