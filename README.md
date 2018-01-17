# DMASON
DMASON is a parallel version of the [MASON](http://cs.gmu.edu/~eclab/projects/mason/) library for writing and running simulations of Agent based simulation models.

Agent-based simulation models are an increasingly popular tool for research and management in many, different and diverse ﬁelds. In executing such simulations the “speed” is one of the most general and important issues. The traditional answer to this issue is to invest resources in deploying a dedicated installation of dedicated computers. D-MASON is a parallel version of the MASON, a library for writing and running Agent-Based simulations. D-MASON is designed to harness unused PCs for increased performances.

## Why Distributed MASON?
Among the motivations to our focus on distributing the simulation on several machines, we can underline how the need for efficiency among the Agent-Based modeling tools is well recognized in literature: many reviews of state-of-the-art frameworks place “speed” upfront as one of the most general and important issues. While a consistent work has been done to allow the distribution of agents on several computing nodes, our approach here is different in principle: we want to introduce the distribution at the framework level, so that the scientists that use the framework (domain experts but with limited knowledge of computer programming and systems) can be unaware of such distribution.

### Changelog DMASON 3.2
1. [Changelog list](https://github.com/isislab-unisa/dmason/blob/master/CHANGELOG.md)

# Build DMASON

    $ mvn clean package


		target	
		├── archive-tmp
		├── classes
		│   └── it
		├── dmason-3.0.1-lib.jar
		├── dmason-global-viewer-3.0.1-jar-with-dependencies.jar
		├── dmason-master-3.0.1-jar-with-dependencies.jar
		├── dmason-worker-gui-3.0.1-jar-with-dependencies.jar
		├── dmason-worker-no-gui-3.0.1-jar-with-dependencies.jar
		├── examples
		│   └── resources
		│   ├── dmason-test-ant-forage-3.0.1-jar-with-dependencies.jar
		│   ├── dmason-test-ant-forage-thin-3.0.1-jar-with-dependencies.jar
		│   ├── dmason-test-flcokers-3.0.1-jar-with-dependencies.jar
		│   ├── dmason-test-flcokers-state-memory-3.0.1-jar-with-dependencies.jar
		│   ├── dmason-test-flcokers-thin-3.0.1-jar-with-dependencies.jar
		│   ├── dmason-test-network-3.0.1-jar-with-dependencies.jar
		│   ├── dmason-test-particles-3.0.1-jar-with-dependencies.jar
		│   └── dmason-test-particles-thin-3.0.1-jar-with-dependencies.jar
		├── generated-sources
		│   └── annotations
		├── javadoc
		├── javadoc-bundle-options
		├── maven-archiver
		│   └── pom.properties
		├── maven-status
		│   └── maven-compiler-plugin
		└── resources
		 ├── app
		 ├── batch
		 ├── examples
		 ├── files
		 ├── image
		 ├── javadoc-style
		 ├── sigar
		 ├── systemmanagement
		 └── util

Generate build without runs test suite:

	$ mvn -Dmaven.test.skip=true clean package 

# Contributors
This section is intended for those who want to contribute this project.  You can ask us more informations and how to contribute at [isislab.unisa@gmail.com](isislab.unisa@gmail.com)([www.isislab.it](http://www.isislab.it)) or on [GitHub](https://github.com/isislab-unisa).

## Add additional library to repository
If you need a library not included in the DMASON repo, you can add it by including the Maven dependency in the `pom.xml`. If the library is 3rd-Party and is not available on public repository, you can put in the DMASON repository.
The first step is generate the dependency Maven data, in the fallowing there is an example of that using the `MASON` library:

    $  mvn install:install-file -DgroupId=it.isislab.dmason -DartifactId=mason.18.jar -Dversion=18 -Dfile=./repository/jars/mrjadapter-1.2.jar -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=./repository  -DcreateChecksum=true
    
After that you must put the output data in the `repository` folder of DMASON GitHub and add to `pom.xml`:

    <dependency>
		<groupId>it.isislab.dmason</groupId>
		<artifactId>mason.18.jar</artifactId>
		<version>18</version>
	</dependency>


### License
Copyright ISISLab, 2018 Università degli Studi di Salerno.

Licensed under the Apache License, Version 2.0 (the "License"); You may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
