#Circle
The circles benchmark model is a benchmark model originating from FLAME and FLAME GPU. It consists of a number of points which exert a repulsive and potentially attractive force over a limited range. Over a number of iterations the model will exhibit a force resolution which will ultimately result in a stable state. The model has origins in a number of biological systems and is analogous to that of cellular interactions of simple swarm systems.

The model has been used for extensive benchmarking of the FLAME and FLAME GPU frameworks and is excellent for testing message acceleration structures for spatially distributed models.


The position __x__ of a circle agent ****_i_**** at a discrete time step **_t−1_** is given by:

_**x<sub>i(t+1)</sub>= x<sub>i(t)</sub> + F<sub>i</sub>**_

where _**F<sub>i</sub>**_ denotes the force exerted on agent **_i_** calculated as:
