#!/bin/bash

echo "StarCluster DMASON cluster builder script by Przemyslaw Szufel & ... guys add your names"
echo "This code is licensed under the terms of AGPL open source license"


echo "This script will configure your DMASON cluster enviroment"
echo "If a currentconfiguration exits it will be OVERWRITTEN and dmasonclusterkey key files will be (re)created"
read -r -p "Are you sure? [y/N] " response
if [[ $response =~ ^([yY][eE][sS]|[yY])$ ]]
then
    echo ""
else
    exit
fi


if [ ! -f credentials.csv ];then

    echo "Credentials.csv file not found"
    echo "In order to obtaint credentials.csv please follow the steps"
    echo "1. Go to https://console.aws.amazon.com/ "
	echo "2. Select >IAM< in the >services< menu and go to >users<"
	echo "3. Select an existing user or create a new one (please not that the user needs to have a permission to launch new EC2 instances)"
	echo "4. Click >Manage Access Keys< and >Create Access Key<"
	echo "5. Click >Download credentials< and save credentials.csv"
	echo "6. Copy credentials.csv to your home directory $HOME and run the script again"
	exit
fi

while IFS="," read f1 f2 f3 
do 
   echo "Reading credentials.csv $f1 $f2 $f3" 
done < credentials.csv

sudo apt-get update
sudo apt-get -y install build-essential python-dev python-setuptools mc
sudo easy_install StarCluster

mkdir ~/.starcluster

#python -c 'from starcluster.templates import config;from os.path import expanduser;f=open(expanduser("~")+"/.starcluster/config","w");f.write(config.config_template);f.close()'
#chmod 600 ~/.starcluster/config

rm -f ~/.starcluster/config
wget -P ~/.starcluster/ https://raw.githubusercontent.com/isislab-unisa/dmason/master/install/cluster/config

mkdir ~/.starcluster/plugins
rm -f ~/.starcluster/plugins/dmason.py
wget -P ~/.starcluster/plugins/ https://raw.githubusercontent.com/isislab-unisa/dmason/master/install/cluster/plugins/dmason.py

uncomment() {
    sed -i "s/^# $1/$1/g" ~/.starcluster/config
}
setconfig() {
    sed -i "s/^#$1 /$1/g" ~/.starcluster/config
    sed -i "s#^\($1\s*=\s*\).*\$#\1$2#" ~/.starcluster/config	
}


setconfig AWS_ACCESS_KEY_ID $f2
setconfig AWS_SECRET_ACCESS_KEY $f3
setconfig AWS_USER_ID ${f1//\"/}


if [[ $(starcluster lk) =~ "dmasonclusterkey" ]];then
  yes | starcluster removekey dmasonclusterkey  
  yes | rm ~/.ssh/dmasonclusterkey.rsa  
fi

starcluster createkey dmasonclusterkey -o ~/.ssh/dmasonclusterkey.rsa

echo DMASON cluster setup completed. Now you can run 
echo starcluster start dmason_cluster_name 
echo In order to connect to the DMASON cluster you need to edit permissions of the security group