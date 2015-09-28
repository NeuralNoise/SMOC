-------------------------------------------------------------------------------
Running functional tests for the web
-------------------------------------------------------------------------------

1. Start your tomcat and deploy the latest web application there.
2. Run
    ant run_funtional_tests
   or start the test from your favorite IDE.
3. If you wish to test a new version of your web application just redeploy it and go back to step 2.
You can run that step however long you want.

If you need to change the address of the tomcat you are testing that can be done modifying
funcTest/bg.smoc.TestConstants#BASE_URL .

That way you can also run the tests against a live instance. However mind that the test DO MODIFY the data.


-------------------------------------------------------------------------------
Running grader tests
-------------------------------------------------------------------------------
Unfortunately as of the moment if you want to run the tests for the grader you must submit them via
the web interface and observe the result.
