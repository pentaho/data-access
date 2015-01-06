This folder contains scripts for doing automated data-access build validation testing.

The primary script, build.sh will compile data-access 1000 times (configurable in build.sh) and perform the gwt-validation check.

Build status, PASS or FAIL are logged for each iteration along with the current overall build success rate.

The output from each iteration is stored in the output subdirectory and in a file corresponding to the iteration.

For example, build-test/output/1.txt
This is the output for the first iteration.  If you see any failures, this will make it much easier to check the logs to determine
what caused the failure.

There is another build script, rebuild.sh which is a convenience build script that will rebuild metadata and the modeler projects and
publish them locally so that data-access can build against the latest changes.  During development, this was a huge pain point since
publish-local was not working correctly unless we manually cleaned out portions of the ivy cache.



Example execution:

```
mdamour@mike6540:build-test$ ./build.sh
Resolving data-access...
Iteration 1: PASS    SUCCESS RATE: 100.000%
Iteration 2: PASS    SUCCESS RATE: 100.000%
Iteration 3: PASS    SUCCESS RATE: 100.000%
Iteration 4: PASS    SUCCESS RATE: 100.000%
Iteration 5: PASS    SUCCESS RATE: 100.000%
Iteration 6: PASS    SUCCESS RATE: 100.000%
Iteration 7: PASS    SUCCESS RATE: 100.000%
Iteration 8: PASS    SUCCESS RATE: 100.000%
Iteration 9: PASS    SUCCESS RATE: 100.000%
Iteration 10: PASS    SUCCESS RATE: 100.000%
```

If you want to test a build that has failed, you will have to first turn on the HALT_ON_ERROR flag in the build.sh.  Upon failure,
script execution will stop.  At this point there is no "dist" from the build, so you'll have to call ant dist at the root of the
data-access project.  Replace the data-access plugin in pentaho-solutions/system with the dist zip.

