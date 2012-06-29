---
layout: documentation
title: Installation
mainmenukeyoverride: installation
---

### Maven
Add the following dependency to your ```pom.xml``` file

	<dependency>
		<groupId>org.twinkql</groupId>
		<artifactId>twinkql</artifactId>
		<version>{% include current-version.html %}</version>
	</dependency>

### Build from source

#### Prerequisites
 * Git
 * Maven
 * Java

Before attempting to build Twinkql from source, make sure you have the above prerequisites available on your system.

#### Cloning from Git

To clone the repository from the Github repository use a Git 'clone' command:

	git clone git://github.com/cts2/twinkql.git

#### Building with Maven

Navigate to the resulting 'twinkql' directory and use the Maven 'install' command:

	mvn clean install

NOTE: You will need an active internet connection in order to successfully build

#### Build artifacts

Build artifacts will be in the '/target' directory.
