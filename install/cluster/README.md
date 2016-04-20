# How to set up a cluster template
These "templates" are a collection of settings that define a single cluster configuration and are used when creating and configuring a High Performance Computing cluster test. You can change which template to use when creating your cluster using the -c option to the start command. For example
```sh
 $ starcluster start -c 2xc3.large mycluster
```

# SPOTTED CLUSTERS
##### SPOT_BID = 0.02 $

*Instance Types Features are avaiable on  https://aws.amazon.com/ec2/instance-types/*


## c3.large templates

```sh
# This cluster template makes a spot cluster having 2 x c3.large node instance. 

[cluster 2xc3.large]
EXTENDS=smallcluster
CLUSTER_SIZE = 2
NODE_INSTANCE_TYPE = c3.large
MASTER_INSTANCE_TYPE = c3.large
AVAILABILITY_ZONE = us-east-1a
SPOT_BID = 0.02

[cluster 4xc3.large]
EXTENDS=2xc3.large
CLUSTER_SIZE = 4

[cluster 8xc3.large]
EXTENDS=2xc3.large
CLUSTER_SIZE = 8

[cluster 16xc3.large]
EXTENDS=2xc3.large
CLUSTER_SIZE = 16
```
## c3.xlarge templates
```sh
[cluster 2xc3.xlarge]
EXTENDS=smallcluster
CLUSTER_SIZE = 2
NODE_INSTANCE_TYPE = c3.xlarge
MASTER_INSTANCE_TYPE = c3.xlarge
AVAILABILITY_ZONE = us-east-1a
SPOT_BID = 0.02

[cluster 4xc3.xlarge]
EXTENDS=2xc3.xlarge
CLUSTER_SIZE = 4

[cluster 8xc3.xlarge]
EXTENDS=2xc3.xlarge
CLUSTER_SIZE = 8

[cluster 16xc3.xlarge]
EXTENDS=2xc3.xlarge
CLUSTER_SIZE = 16
```

# FREE TIER CLUSTERS
### ONLY t2.micro INSTANCES
```sh
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
EXTENDS=2xt2.micro
CLUSTER_SIZE = 8

[cluster 16xt2.micro]
EXTENDS=2xt2.micro
CLUSTER_SIZE = 16
```


# Use conditions
For free AWS account using an instance type different from a t2.micro will involve a payment. If you are a student or a some members of a Instruction Institute, you can require a educational account which will provide you few credits to start.
