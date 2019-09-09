### related commands

To generate reports for JDepend and Spotbugs run **mvn site**

To run tests and generate Jacoco report run **mvn test**

To create the jar files run **mvn package**

### server QA
1. **Jacoco gaps**<br> Over all it appears my test coverage has an extremely poor overall performance of only 15%. This is because the bulk of this package was the GUI and the CreateRandomLogs classes which were not required to be tested.

  - LogEvent 100%

  - Resthome4LogsAppender 93% <br>The remaining 7% is due to two a Catch statement which was never entered. The first catch was for the URI builder, the URI we were creating never changed and was known to be correct, so it never threw an exception. The second part catch never entered was for sending the LogEvent to the server. This would only fail if the server was not running, however, never entered as throughout the tests it was assumed that the server was running. The final part of code not covered was the requiresLayout() which was to be implemented from the abstract class AppenderSkeleton, the method was never used.

  -all other classes had 0% as they were not tested. They were either apart of the GUI or the CreateRandomLogs classes.

2. **JDepend**<br> My Client package has distance of 0. It aligns with Mavens "Ideal Package", it is a pure implementation. It is completely concrete and has an instability of 1.

3. **Spotbugs**<br> No bugs.
