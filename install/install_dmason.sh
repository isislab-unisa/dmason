#!/bin/bash

if [ $(id -u) -ne 0 ]; then
    echo "You have to run this script as root"
    exit -1
fi


# create current directory
mkdir dmason
cd dmason

#download java and set JAVA_HOME env
wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u73-b02/jdk-8u73-linux-x64.tar.gz
tar xzvf jdk-8u73-linux-x64.tar.gz
mkdir -p "$HOME/java"
cp -r jdk1.8.0_73 "$HOME/java" 
JAVA_HOME="$HOME/java/jdk1.8.0_73"
chown -R $SUDO_USER:$SUDO_USER "$HOME/java"
echo "installing jdk in $JAVA_HOME"
export JAVA_HOME
export PATH=$PATH:$JAVA_HOME

update-alternatives --install "/usr/bin/java" "java" "$JAVA_HOME/bin/java" 1 
update-alternatives --set "java" "JAVA_HOME/bin/java" 
update-alternatives --install "/usr/bin/javac" "javac" "$JAVA_HOME/bin/javac" 1 
update-alternatives --set "javac" "$JAVA_HOME/bin/javac"
update-alternatives --install "/usr/bin/javaws" "javaws" "$JAVA_HOME/bin/javaws" 1 
update-alternatives --set "javaws" "$JAVA_HOME/bin/javaws"

#download maven and set MAVEN_HOME env
wget http://apache.panu.it/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz#
tar xzvf apache-maven-3.3.9-bin.tar.gz
mkdir -p "$HOME/maven"
cp -r apache-maven-3.3.9 "$HOME/maven"
MAVEN_HOME="$HOME/maven/apache-maven-3.3.9/"
chown -R $SUDO_USER:$SUDO_USER "$HOME/maven"
export PATH=$PATH:$MAVEN_HOME:$MAVEN_HOME/bin


#download dmason and unzip project
#alpha version 
#wget https://github.com/isislab-unisa/dmason/archive/master.zip
#unzip master.zip
wget https://github.com/isislab-unisa/dmason/archive/DMASON3.1-alpha.zip
unzip DMASON3.1-alpha.zip


# run maven install on dmason project 
cd dmason-DMASON3.1-alpha
mvn -Dmaven.test.skip=true clean package

#create folder of executing environment
mkdir -p "$HOME/dmason"

cp -r target/DMASON-3.1.jar "$HOME/dmason"
cp -r target/resources "$HOME/dmason"
chown -R $SUDO_USER:$SUDO_USER "$HOME/dmason"

#remove current directory CONTRolla
cd ../..
rm -r dmason
#display folder
echo "DMASON was installed in $HOME/dmason"
