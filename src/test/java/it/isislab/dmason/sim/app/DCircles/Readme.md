#Circle
The circles benchmark model is a benchmark model originating from FLAME and FLAME GPU. It consists of a number of points which exert a repulsive and potentially attractive force over a limited range. Over a number of iterations the model will exhibit a force resolution which will ultimately result in a stable state. The model has origins in a number of biological systems and is analogous to that of cellular interactions of simple swarm systems.

The model has been used for extensive benchmarking of the FLAME and FLAME GPU frameworks and is excellent for testing message acceleration structures for spatially distributed models.


The position __x__ of a circle agent ****_i_**** at a discrete time step **_t−1_** is given by:

_**x<sub>i(t+1)</sub>= x<sub>i(t)</sub> + F<sub>i</sub>**_

where _**F<sub>i</sub>**_ denotes the force exerted on agent **_i_** calculated as:

![](https://github.com/isislab-unisa/dmason/blob/master/src/test/java/it/isislab/dmason/sim/app/DCircles/summary.png)
 

The parameter _**r**_ is the homogeneous radius of the circle agent. The square Iverson bracket notation determines a condition for both the repulsive forces _**F <sup>rep</sup><sub>ij</sub>**_ and attractive 
force _**F <sup>attr</sup><sub>ij</sub>**_ between the agent _**i**_ and a neighboring agent _**j**_ . When the condition evaluates to true it returns a value of **1** otherwise the value returned is **0** . 
The value _**d<sub>ij</sub>**_ is the distance between agent positions _**x<sub>i</sub>**_ and _**x<sub>j</sub>**_ given by:

![](https://github.com/isislab-unisa/dmason/blob/master/src/test/java/it/isislab/dmason/sim/app/DCircles/distance.png)

The repulsive and attractive forces as defined as follows:

![](https://github.com/isislab-unisa/dmason/blob/master/src/test/java/it/isislab/dmason/sim/app/DCircles/repforce.png) 

![](https://github.com/isislab-unisa/dmason/blob/master/src/test/java/it/isislab/dmason/sim/app/DCircles/attrforce.png)

The parameters **k<sub>rep</sub>** and **k<sub>attr</sub>**  are the repulsive and attractive damping terms respectively.

The initial conditions of the circles model are a randomly positioned set of circle agents. The random distribution should be linear and the following parameters should be used to benchmark the model.


**W** The width and height of the environment in which agents are placed

**ρ**  The density of agents within the environment (this will dictate the fixed number of agents)

##Model Parameters


* **k<sub>rep</sub>** The repulsive damping term. Increasing this value will encourage agents to separate
* **k<sub>attr</sub>** The attractive damping term. Increasing this term will encourage agents to attract
* **r** he interaction radius of the circle agents. Increasing this value will increase the communication between agents (assuming constant density)
* **ρ** The density of agents within the environment. Increasing this value will increase the communication within the model
* **W** The width and height of the environment in which agents are placed. Increasing the environment size is equivalent to increasing the problem size N (i.e. the number of agents) given a fixed _ρ_
.