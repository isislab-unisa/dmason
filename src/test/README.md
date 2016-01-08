# Test Suite Contents
New version of this file must be edited.
<!---	.
	├── sim
	│   ├── app
	│   │   └── DFlockers
	│   │       ├── DFlocker.java
	│   │       ├── DFlockers.java
	│   │       ├── DFlockersWithUI.java
	│   │       ├── icon.png
	│   │       ├── icoRed.png
	│   │       ├── index.html
	│   │       ├── package.html
	│   │       ├── RemoteFlock.java
	│   │       └── TestDFlockers.java
	│   ├── engine
	│   │   └── DistributedStateConnectionJMSTester.java
	│   └── field
	│       ├── CellTypeTester.java
	│       ├── continuous
	│       │   ├── DContinuous2DFactoryTester.java
	│       │   ├── DContinuous2DXYTester.java
	│       │   ├── loadbalanced
	│       │   │   └── DContinuous2DXYLBTester.java
	│       │   ├── region
	│       │   │   ├── RegionDoubleLBTester.java
	│       │   │   └── RegionDoubleTester.java
	│       │   └── thin
	│       │       └── DContinuous2DXYThinTester.java
	│       ├── grid
	│       │   ├── numeric
	│       │   │   ├── DDoubleGrid2DFactoryTester.java
	│       │   │   ├── DDoubleGrid2DXYTester.java
	│       │   │   ├── DIntGrid2DFactoryTester.java
	│       │   │   ├── DIntGrid2DXYTester.java
	│       │   │   ├── loadbalanced
	│       │   │   │   ├── DDoubleGrid2DXYLBTester.java
	│       │   │   │   └── DIntGrid2DXYLBTester.java
	│       │   │   ├── region
	│       │   │   │   ├── RegionDoubleNumericTester.java
	│       │   │   │   └── RegionIntegerNumericTester.java
	│       │   │   └── thin
	│       │   │       ├── DDoubleGrid2DXYThinTester.java
	│       │   │       └── DIntGrid2DXYThinTester.java
	│       │   ├── region
	│       │   │   ├── RegionIntegerLBTester.java
	│       │   │   └── RegionIntegerTester.java
	│       │   └── sparse
	│       │       ├── DSparseGrid2DXYTester.java
	│       │       ├── loadbalanced
	│       │       │   └── DSparseGrid2DXYLBTester.java
	│       │       └── thin
	│       │           └── DSparseGrid2DXYThinTester.java
	│       ├── support
	│       │   └── field2D
	│       │       └── UpdateMapTester.java
	│       └── UpdateCellTester.java
	└── testsuite
	    └── TestSuite.java

# Bug detected (last update 20/10/2015)
------------------------------------------------------------------

###R1 [dmason.sim.field.continuous.region.RegionDouble]###
1. The createRegion method use at the return the instance variables width and heigth while should use variables WIDTH and heigth passed as parameters.

###R2 [dmason.sim.field.continuous.region.RegionDoubleLB]###
1. The same mistakes of RegionDouble

### R3 [dmason.sim.field.UpdateCell]###
1. When using the method getUpdates (step, numUpdates) returns a queue of numUpdates elements. The method takes the queue of the step indicated and copying in temp. To verify that you are copying the desired items in temp using the code:



	while (tmp.size ()! = Num_updates) {...}

and this has the following bug:

	1. Infinite loop if numUpdate>queue size

###R4 [dmason.sim.field.continuous.DContinuous2DFactory]###
In the method createContinuous2DXY problems are found in the blocks of code that implement the following procedures:

1. Instance variables my_width and my_height are not properly instantiated.

###R5 [dmason.sim.field.grid.numeric.DDoubleGrid2DFactory]###
1. The same errors indicated for DContinuous2DFactory, except for the mode SQUARE_BALANCED_DISTRIBUTION.

###R6 [dmason.sim.field.support.field2D.UpdateMap]###
1. Identical errors at UpdateCell  (due to copy / paste).

### R7 [dmason.sim.field.grid.numeric.DIntGrid2DFactory]
1. The same errors indicated for DContinuous2DFactory.

###R8 [dmason.sim.field.continuous.DContinuous2DXY]###
1. The method getAllVisibleAgent is never used in DMASON, and still would not work because it forces the use of clone on an interface, then launching a InstantiationException.(Used by SociallyDmagingBehaviours only) 
2. ResetAddAll used by SociallyDamagingBehaviour only.
3. SetDistributedObjectLocation.
	1. If you pass an agent already exists, this is not moved, but creates another agent equal to a new location.
	2. With a stress-test (numLoop> 8) manifests a bug due to the representation of numbers in java.
4. getNumAgents.
	1. An agent positioned in overlapping regions for the same simulation step will be counted more than once
	2. In the same simulation step when an agent moves from a region to another one the agent will be counted twice if the regions belong to the same DistributedField and don't overlap 

###R9[dmason.sim.engine.DistributedStateConnectionJMS]###
1. Mode HorizontalDistributionMode in a space not toroidal is created topic 0-lastCellR, this is semantically incorrect because having to do with a space not toroidal, it is not possible that there is a cell that sign up to the topic in question.
2. There is a behavior where some tests exceeded usually fail because the subscribed is made to erroneous topics.
3. HorizontalDistributionMode mode in toroidal space is not created the topic 0-0L, being annulus, it is necessary that this topic exists, otherwise it will be impossible to manage the migration of agents between 0-lastCellR and 0-0L.
4. Mode squareDistributionMode in a space not toroidal in pos (1-1) with three columns and two rows is created by topic D (down) that should not be there.
5. In the mode squareDistributionMode in a space not toroidal in pos (1-1) with two columns and three rows is created by topic R (Right) that should not be there.
6. In the mode squareDistributionMode in a space not toroidal in pos (0-1) with two columns and two rows is created by topic R (Right) that should not be there.
7. In the mode squareDistributionMode in a space not toroidal in pos (1-1) with two columns and two rows is created by topic R (Right) that should not be there.
8. In the mode HorizontalDistributionMode in a toroidal space, is not handled properly subscription to topic neighbors when the frame size is 1x2.
9. In the Horizontal mode distribution mode toroidal not, the cell is enrolled in a topic right and that also refers to a cell that does not exist when the frame size is 1x2.

###R10###
The classes DDoubleGrid2DXYThin, DIntGrid2DXYThin, DDoubleGrid2DXY, DIntGrid2DXY, DSparseGrid2DXY and DSparseGrid2DXYThin have in common the same bug, or are not backed up positions on the outer sides of the field when the field is toroidal.


Conceptual improvements
----------------------

###MC1###
The various MODE that indicates how the partitioning of the field, have been written in all classes of type factory. It would be better to write them all in a single class.

###MC2###
Whenever you create instances of the simulation, must be of DistributedState and not of SimState. In all classes of type factory and 2DXY has highlighted this problem.

###MC3###
In DContinuous2DXY (and also for all other classes like) I have found that in the HashMap content of corner that are no longer used and then instantiated in the current version (and all subsequent) of DMASON. To avoid ambiguity recommend to remove them. The corners are:

1. corner_out_up_left_diag_left
2. corner_out_up_right_diag_up
3. corner_out_down_right_diag_down
4. corner_out_down_left_diag_down
5. corner_out_up_right_diag_right
6. corner_out_up_left_diag_up
7. corner_out_down_left_diag_left
8. corner_out_down_right_diag_right

Modifications for the testing
----

###MT1###
In the class DistributedStateConnectionJMS order to better support the stub was necessary to initialize the connection in the connection method instead of in the constructor. Some tests therefore require this change, it would be advisable to check that it will not create problems and can be made, if not it is appropriate to remember before starting the test suite.
###MT2###
In the class DistributedStateConnectionJMS you need to make a change in order to perform the corresponding test, or create a new constructor that receives as a parameter a connection developed ad hoc. This is because all the variables and methods used in the class are private, and therefore would not be possible to verify them. I apply this method because I consider less intrusive than having to change from private to public all the variables and all the necessary methods, and also avoids the use of ActiveMQ.
###MT3###
It was necessary to change the type of class Trigger connection used, from ConnectionNFieldsWithActiveMQAPI to ConnectionJMS so that there are no problems cast when using the mechanism shown in step MT2.
###MT4###
To prepare the system to a possible use of jMock was added a constructor to the class DistributedStateConnectionJMS receiving an additional parameter, namely a ConnectionJMS. This way you can use jMock to intercept function calls by the class against the connection.
###MT5###
We have developed a way to perform the test of a simulation. To do this we must take a simulation existing copy in package testing and change the constructor. Also within the start method is necessary to include the following as the first line:


	((DistributedStateConnectionFake)super.getDistributedStateConnectionJMS()).setupfakeconnection(this);
###MT6###
In the class DistributedState was added a new constructor to run tests in a simulation runtime.
###MT7###
In the class ConnectionType Added variable fakeUnitTestJMS.
###MT8###
In the class DistributedStateConnectionJMS added an empty constructor in order to support the testing of simulations at runtime.

Bugs solution
----

###SB1 [R8.4.1]###
If I wanted to update the position of the agent by invoking the method setDistributedObjectLocation would spend the agent and the new position as parameters, I would expect that the agent moves from the old to the new location and that elements within dell'rmap remain the same. What happens instead is that the elements nell'rmap increase and that the same agent is simultaneously in two different positions: the new one and the old one. This is because there is no control in the Region.
In the method RegionDouble addAgents it would take control:

	if(this.contains(e))this.remove(e); 
then later make the add (e) or add the agent who was eliminated, but the new location.
To implement this function, you have to insert the equals method in the class entry.
###SB2 [R8.4.2]###
There may be abnormal behavior in the displacement of an agent: since the expression (0.99999999999999995 <1) returns false, an agent very close to the limit of a region would be brutally moved to another region. It is understood that difficult a thing, but wanting to be picky would need a mechanism to avoid this kind of problem.
###SB3 [R4.1.2]###
The error is due to a kind used for the operation of division: safezone and 2 are integers, then the operator / returns an integer value by default. To solve the problem just replace (safezone / 2) with (safezone / 2.0).
###SB4 [R4.1]###
It would be appropriate to throw exceptions of type DMasonException before they are made divisions by zero or created field conceptually incorrect.
###SB5 [R8.5]###
It would be appropriate to throw exceptions to prevent the user is given the opportunity to provide input on the type indicated, otherwise the results (of course inconsistent) would make the work of the unconscious user inconsistent.
###SB6 [R4.2.1]###
The variable **safezone** was declared Integer, so the operation **/** returned a integer casted value
-->
