# data-access #
Pentaho Data Access Wizard

#### Pre-requisites for building the project:
* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://bintray.com/pentaho/public-maven/download_file?file_path=settings.xml) in your <user-home>/.m2 directory

#### Building it

__Build for nightly/release__

All required profiles are activated by the presence of a property named "release".

```
$ mvn clean install -Drelease
```

This will build, unit test, and package the whole project (all of the sub-modules). The resulting data-access-plugin (for pentaho-server)
artifact will be generated in: ```assemblies/data-access-plugin/target```

__Build for CI/dev__

The `release` builds will compile the GWT module uglified and for all supported browsers.
To build without the uglified version of the GWT module (you get the PRETTY version)... just eliminate the `release` property.

```
$ mvn clean install
```

Additionally, you can speed up the build by producing GWT for only for a specified browser (or multiples).
You can do this by another property, `gwt.user.agent`. Valid values are `safari`, `ie9`, `ie8`, and `gecko1_8`.

##### build for safari/chrome
```
$ mvn clean install -Dgwt.user.agent=safari
```
##### build for firefox
```
$ mvn clean install -Dgwt.user.agent=gecko_1_8
```
##### build for IE
```
$ mvn clean install -Dgwt.user.agent=ie8,ie9
```


#### Running the tests

__Unit tests__

This will run all tests in the project (and sub-modules).
```
$ mvn test
```

If you want to remote debug a single java unit test (default port is 5005):
```
$ cd core
$ mvn test -Dtest=ConditionTest -Dmaven.surefire.debug
```

__Integration tests__
In addition to the unit tests, there are integration tests in the core project.
```
$ mvn verify -DrunITs
```

To run a single integration test:
```
$ mvn verify -DrunITs -Dit.test=GeoContentGeneratorIT
```

To run a single integration test in debug mode (for remote debugging in an IDE) on the default port of 5005:
```
$ mvn verify -DrunITs -Dit.test=DataDourcePublishIT -Dmaven.failsafe.debug
```

__IntelliJ__

* Don't use IntelliJ's built-in maven. Make it use the same one you use from the commandline.
  * Project Preferences -> Build, Execution, Deployment -> Build Tools -> Maven ==> Maven home directory
