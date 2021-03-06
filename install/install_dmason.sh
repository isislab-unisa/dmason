#!/bin/bash

DMASON_TMP_FOLDER="dmason_install"
SKIP_JAVA_INSTALL=''
SKIP_MAVEN_INSTALL=''
DMASON_HOME="$HOME/dmason"

if [ $(id -u) -ne 0 ]; then
    echo "You have to run this script as root"
    exit -1
fi

while getopts "jm" opt ; do
		
	case $opt in
		j) 
			SKIP_JAVA_INSTALL="true"
			;;
		m)
			SKIP_MAVEN_INSTALL="true"
			;;
	esac
done


# create current directory
mkdir $DMASON_TMP_FOLDER
cd $DMASON_TMP_FOLDER

#download java and set JAVA_HOME env
JAVA_HOME="$HOME/java/jdk1.8.0_73"
if [ -z "$SKIP_JAVA_INSTALL" ]; then
	wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u73-b02/jdk-8u73-linux-x64.tar.gz
	tar xzvf jdk-8u73-linux-x64.tar.gz
	mkdir -p "$HOME/java"
	cp -r jdk1.8.0_73 "$HOME/java" 

	chown -R $SUDO_USER:$SUDO_USER "$HOME/java"
	echo "installing jdk in $JAVA_HOME"

fi

export JAVA_HOME
export PATH=$PATH:$JAVA_HOME

if [ -z "$SKIP_JAVA_INSTALL" ]; then
	update-alternatives --install "/usr/bin/java" "java" "$JAVA_HOME/bin/java" 1 
	update-alternatives --set "java" "JAVA_HOME/bin/java" 
	update-alternatives --install "/usr/bin/javac" "javac" "$JAVA_HOME/bin/javac" 1 
	update-alternatives --set "javac" "$JAVA_HOME/bin/javac"
	update-alternatives --install "/usr/bin/javaws" "javaws" "$JAVA_HOME/bin/javaws" 1 
	update-alternatives --set "javaws" "$JAVA_HOME/bin/javaws"
fi



#download maven and set MAVEN_HOME env
if [ -z "$SKIP_MAVEN_INSTALL" ]; then
	wget http://apache.panu.it/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
	tar xzvf apache-maven-3.3.9-bin.tar.gz
	mkdir -p "$HOME/maven"
	cp -r apache-maven-3.3.9 "$HOME/maven"
	chown -R $SUDO_USER:$SUDO_USER "$HOME/maven"
fi

MAVEN_HOME="$HOME/maven/apache-maven-3.3.9/"
export PATH=$PATH:$MAVEN_HOME:$MAVEN_HOME/bin


#download dmason and unzip project
#master version 
#wget https://github.com/isislab-unisa/dmason/archive/master.zip
#unzip master.zip

#beta version
#wget https://github.com/isislab-unisa/dmason/archive/DMASON3.1-beta.zip
#unzip DMASON3.1-beta.zip

# beta-2 version
wget https://github.com/isislab-unisa/dmason/archive/DMASON3.1-beta.2.zip
unzip DMASON3.1-beta.2.zip

# run maven install on dmason project 
#cd dmason-master

#beta version
#cd dmason-DMASON3.1-beta

#beta-2 version
cd dmason-DMASON3.1-beta.2


mvn -Dmaven.test.skip=true clean package

#create folder of executing environment
mkdir -p "$DMASON_HOME"

cp -r target/DMASON-3.1.jar "$DMASON_HOME"
cp -r target/resources "$DMASON_HOME"
chown -R $SUDO_USER:$SUDO_USER "$DMASON_HOME"

#remove current directory
cd ../..
rm -r $DMASON_TMP_FOLDER
#display folder
echo "DMASON has been installed in $DMASON_HOME"
