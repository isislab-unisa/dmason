# DMASON
DMASON is a parallel version of the [MASON](http://cs.gmu.edu/~eclab/projects/mason/) library for writing and running simulations of Agent based simulation models.

Agent-based simulation models are an increasingly popular tool for research and management in many, different and diverse ﬁelds. In executing such simulations the “speed” is one of the most general and important issues. The traditional answer to this issue is to invest resources in deploying a dedicated installation of dedicated computers. D-MASON is a parallel version of the MASON, a library for writing and running Agent-Based simulations. D-MASON is designed to harness unused PCs for increased performances.

## Why Distributed MASON?

Among the motivations to our focus on distributing the simulation on several machines, we can underline how the need for efficiency among the Agent-Based modeling tools is well recognized in literature: many reviews of state-of-the-art frameworks place “speed” upfront as one of the most general and important issues. While a consistent work has been done to allow the distribution of agents on several computing nodes, our approach here is different in principle: we want to introduce the distribution at the framework level, so that the scientists that use the framework (domain experts but with limited knowledge of computer programming and systems) can be unaware of such distribution.

### Changelog DMASON 3.0.2
1. [Changelog list](https://github.com/isislab-unisa/dmason/blob/master/CHANGELOG.md)
#Compiling DMASON from src/ to target/ using Apache Maven

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
		├── maven-archiver
		│   └── pom.properties
		├── maven-status
		│   └── maven-compiler-plugin
		└── resources
		 ├── app
		 ├── batch
		 ├── file
		 ├── files
		 ├── image
		 ├── javadoc-style
		 ├── systemmanagement
		 └── util

Generate build without runs test suite:

	$ mvn -Dmaven.test.skip=true clean package 

#Contributors
This section is intended for those who want to contribute this project.  You can ask us more informations and how to contribute at [isislab.unisa@gmail.com](isislab.unisa@gmail.com)([www.isislab.it](http://www.isislab.it)) or on [GitHub](https://github.com/isislab-unisa).

##Add additional library to repository

If you need a library not included in the DMASON repo, you can add it by including the Maven dependency in the `pom.xml`. If the library is 3rd-Party and is not available on public repository, you can put in the DMASON repository.
The first step is generate the dependency Maven data, in the fallowing there is an example of that using the `MASON` library:

    $  mvn install:install-file -DgroupId=it.isislab.dmason -DartifactId=mason.18.jar -Dversion=18 -Dfile=./repository/jars/mrjadapter-1.2.jar -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=./repository  -DcreateChecksum=true
    
After that you must put the output data in the `repository` folder of DMASON GitHub and add to `pom.xml`:

    <dependency>
		<groupId>it.isislab.dmason</groupId>
		<artifactId>mason.18.jar</artifactId>
		<version>18</version>
	</dependency>

# DMASON on cluster
To run DMASON on cluster, you can follow these steps 


### Configure Java Development Kit 8 as default on all cluster's nodes
Download Java SE Development Kit 8 from oracle site. You can follow this instructions https://github.com/serfla/jdk-cluster-installer to set JDK 8 as default. 
Repeat this operation for all nodes of clusters.

### Generate DMASON's jar

Run maven install of DMASON project. Under **target** folder you can find the following files:
- *DMASON-3.1.jar*
- *resources* folder
 
You can create a folder called DMASON with these files. Now copy this folder on all nodes of cluster. 

### Configure Activemq

On master node editing Apache ActivemQ configuration file 'resources/systemmanagement/master/conf/config.properties' in order to set ip and port of Apache ActivemQ:
- set ip   (default is 127.0.0.1), the same ip of master node
- set port (default is 61616)

### Run DMASON on cluster

On master node open a terminal e launch this command to start the master node

```sh
$ java -jar DMASON-3.1.jar -m master
```
In your web browser enter the address *http://127.0.0.1:8080* to start a Web UI of system management, if master node is on 127.0.0.1. You have to use latest version of *Google Chrome* or *Firefox* as web browser.   

Now, to start the slaves node you can open a new terminal on the master node and launch this command

```sh
#M is the max number of available cells that the slave nodes can execute
$ java -jar DMASON-3.1.jar -m worker -ip <ipactivemq> -p <portactivemq> -h <ipslave1 ipslave2 ... ipslaveN> -ns <M>
```
or you can launch this command on each slave node 

```sh
#M is the max number of available cells that the slave node can execute
$ java -jar DMASON-3.1.jar -m worker -ip <ipactivemQ> -p <portActivemq> -ns M
```

where M is the max number of available cells that the slave nodes can execute.

#Release notes

### Apache ActiveMQ notes

Please use the Apache ActiveMQ [5.13.3] (http://activemq.apache.org/activemq-5133-release.html) version, or version greater than or equal to 5.12.2.

### License
Copyright ISISLab, 2016 Università degli Studi di Salerno.

Licensed under the Apache License, Version 2.0 (the "License"); You may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
