

#######################################
######### SPOTTED CLUSTERS ############
######### SPOT_BID = 0.02  ############
#######################################

# Instance Types Features are avaiable on 
# https://aws.amazon.com/ec2/instance-types/

######################################## 
######### c3.large templates ###########
########################################
[cluster 2xc3.large]

# This cluster template makes a spot cluster having 2 x c3.large node instance. 
#Bid value increasing $0.02

EXTENDS=smallcluster
CLUSTER_SIZE = 2
NODE_INSTANCE_TYPE = c3.large
MASTER_INSTANCE_TYPE = c3.large
AVAILABILITY_ZONE = us-east-1a
#SPOT_BID = 0.02

[cluster 4xc3.large]
EXTENDS=2xc3.large
CLUSTER_SIZE = 4

[cluster 8xc3.large]
EXTENDS=2xc3.large
CLUSTER_SIZE = 8

[cluster 16xc3.large]
EXTENDS=2xc3.large
CLUSTER_SIZE = 16


######################################## 
######### c3.xlarge templates ##########
########################################

[cluster 2xc3.xlarge]
EXTENDS=smallcluster
CLUSTER_SIZE = 2
NODE_INSTANCE_TYPE = c3.xlarge
MASTER_INSTANCE_TYPE = c3.xlarge
AVAILABILITY_ZONE = us-east-1a
#SPOT_BID = 0.02

[cluster 4xc3.xlarge]
EXTENDS=2xc3.xlarge
CLUSTER_SIZE = 4

[cluster 8xc3.xlarge]
EXTENDS=2xc3.xlarge
CLUSTER_SIZE = 8

[cluster 16xc3.xlarge]
EXTENDS=2xc3.xlarge
CLUSTER_SIZE = 16

#######################################
######### FREE TIER CLUSTERS ##########
###### ONLY t2.micro INSTANCES ########
#######################################

[cluster 2xt2.micro]
EXTENDS=smallcluster
CLUSTER_SIZE = 2
NODE_INSTANCE_TYPE = t2.micro
MASTER_INSTANCE_TYPE = t2.micro
AVAILABILITY_ZONE = us-east-1a

[cluster 4xt2.micro]
EXTENDS=2xt2.micro
CLUSTER_SIZE = 4

[cluster 8xt2.micro]
EXTENDS=2xt3.micro
CLUSTER_SIZE = 8

[cluster 16xt2.micro]
EXTENDS=2xt3.micro
CLUSTER_SIZE = 16
