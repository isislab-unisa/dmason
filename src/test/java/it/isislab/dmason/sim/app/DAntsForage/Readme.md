#Ants Foraging
This is the distributed version of AntsForaging, default application of Mason package

This is a simulation of artificial ants foraging from a nest, discovering a food source in the face of obstacles, and then establishing a trail between the nest and food source. The model uses two pheromones which set up gradients to the nest and to the food source respectively. The pheromones evaporate as well. You can change some of these constants.

A two-pheromone model is likely not biological, but this model is based on a series of papers we did showing a strong relationship between ant pheromone algorithms and dynamic programming.
balanced
For more information about the model you can read the following studies:
* [A Pheromone-Based Utility Model for Collaborative Foraging](http://cs.gmu.edu/~eclab/papers/panait04pheromone.pdf),Liviu Panait and Sean Luke. In AAMAS 2004
* [Ant Foraging Revisited](http://cs.gmu.edu/~eclab/papers/panait04ant.pdf), Liviu Panait and Sean Luke. In ALIFE 9
* [Learning Ant Foraging Behaviors](http://cs.gmu.edu/~eclab/papers/panait04learning.pdf),Liviu and Sean Luke. In ALIFE 9

At the beginning of simulation all ants born from the nest, designed by a point in a field of partition, so the load is not balanced 