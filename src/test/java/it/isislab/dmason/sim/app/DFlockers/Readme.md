#Flockers

This is a simple example of Boids-style flocking, hastily put together late one night at the SwarmFest 2004 conference. It's much simpler and somewhat faster than our Woims and Woims3D models, which employ sinusoidal movement of multiple worm segments, mostly for fun. Incidentally, Craig Reynolds won a techincal Oscar (Scientific and Engineering) for his Boids algorithm.

In this model, some N flockers flock about in a toroidal landscape. Each flocker moves one step in the direction of a vector it has computed. This vector is the weighted sum of five unit vectors:

* Avoidance: A vector away from other flockers. This is computed as the sum, over all neighbors (dead ones included), of a vector to away from the neighbor, modified so that its magnitued is 1/d the distance to the neighbor (so close neighbors are much stronger a repelling force).
* Cohesion: A vector towards the "center of mass" of nearby flockers. This is computed as the sum, over all live neighbors, of a vector towards the neighbor.
Momentum: A vector in the direction the flocker went last time.
* Coherence: A vector in the direction other flockers are going. This is computed as the sum, over all live neighbors, of the momentum vector of each neighbor.
A random vector.
All these parameters affect the model as it is running. We have pre-weighted these values so that 1.0 for each one results in a nice flock. Try changing the values and see what happens.

Some additional model parameters which only affect newly-started models:

* Number of Flockers: self-explanatory.
* Dead Flocker Probability: the probability that a flocker will be born dead. We include dead flockers to make for interesting obstacles.
* Height and Width: the dimensions of the environment.
* Neighborhood: how many units in each direction a flocker scans to determine nearby flockers. Changing this has two effects. First, a bigger neighborhood results in stronger cohesion and coherence. Second, a bigger neighborhood results in slower computation, because many more neighbors will be considered. The drop-off is O(n^2) in neighborhood distance. You can also see the effect of more neighbors by deleting Avoidance, resulting in the flockers collapsing and the system slowing as tons of flockers fill the same neighborhood region. This provides a good tutorial for understanding the importance of a good discretization setting for Continuous2D and Continuous3D fields in MASON. 

All agents are equally positioned in the distributed fields 