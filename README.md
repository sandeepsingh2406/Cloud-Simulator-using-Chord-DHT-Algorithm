This repo contains the HW4 project i.e. **Cloud Simulator for Chord Algorithm** based on Akka Actors.

Project Members are: Abhijay Patne , Kruti Sharma, Sandeep Singh

-------------------------------------------------------------------------------------------------------
**Highlights and features of the application:**
Apart from the basic requirements of the Chord Simulator, we have also implemented:

Node joining

Node leaving

Transfer of keys

Snapshot at certain interval provided by user

Two different logging framework

Automated concurrent user request simulation which includes addition, deletion and retrieval of movies

Please find details of these bonus implementations later in the readMe.

-------------------------------------------------------------------------------------------------------

Below is a work flow of this project, followed by description of the project structure:

**Project Flow:**

1. The user enters all input parameters through command line. (Described later)

2. The Chord actor system, web service(through Akka HTTP), and user actor system are started. Users are created in accordance to some of the input parameters.

3. The web service is listening on http://localhost:8080 for all kinds of requests-> add user, remove user, add item, lookup item and delete item.

3. Initially, a few random nodes are added to create the ring and add nodes to the ring, use REST calls to our web service.

4. After nodes have been added, the user actors, start making Rest calls to our web service to add/delete/lookup movie items.

5. The user actors keep making calls until the duration of simulation(entered initially as command line argument) finishes.

6. The user actors use Akka logger to log all requests and response. Whereas for the Chord system, the requests/responses are logged through SLF4J.
 
7. Snapshots are taken using the Chord system, after a certain time interval as specified by user. The states of all nodes are logged.

-------------------------------------------------------------------------------------------------------

**Project Structure:**

Scala main classes(/src/main/scala/):

1. **ChordAlgorithm.scala**:


	This file contains the implementation of Chord Algorithm using Akka Actors. Each node in the ring is represented by an Akka Actor.
	The two actors present in this file are: ** ChordMainActor ** and ** CloudNodeActor **. The ChordMainActor is to instantiate the main actor 
	of the cloud and takes in the parameter : total nodes which instantiates CloudNodeActor. The number of actors created for CloudNodeActor is equal
	to the number of nodes as entered by the user which shows the total number of systems in the cloud. These systems are initially inactive.
	In order to activate each node in the ring the requests are sent from the Webservice. If the first node is joining the ring, that it implies we are 
	creating the ring and now we can interact with the node present in the ring. After the first node is added to the ring, any other node can join the
	ring on the basis of any random node active in the ring. Once nodes are added to the ring, the other requests includes - leaving a node, adding an item 
	to the nodes dataset, transfering keys when a new node joins or when a node leaves, stabilization of all nodes when a node joins or leaves and 
	deleting an item from the nodes dataset. All the interations for performing these operations can be done through the webservice.
	
	
	### chordMainMethod ###
	Below are the main functions:
	
	* Main() - this function is called when ChordAlgorithm is run. This accepts the input parameter : No of users, Total System, Min Requests/min, 
	Max Requests/min, SimulationDuration, Simulation Mark, Items requested (accepts a txt file) and Ratio of Read/Write requests. This main method
	is responsible for instantiating the ChordMainActor and which in turn instantiates CloudNodeActor. After the instantiation is complete, it 
	makes a call to the ** mainHandlerService ** to start the web service. After the web service is started, it instantiates the cloud ring with some nodes
	to create the cloud ring and make few nodes active. After this it activates ** MyUserActorDriver ** which simualates all the users request via the web service.
	
	* CreateRingWithNode() - this function is responsible to create the ring for chord with a node. This node is the only node active in the cloud.
	
	* InsertNodeInRing() - this function is responsible to add other nodes in an existing ring. The new node can join the ring on the basis of any active
	node present in the ring. When a new node joins it internally performs operation such as transfer of keys (items), stabilize the ring where all nodes 
	update their finger table.
	
	* LookupItem() - this function is responsible to perform FindSuccessor operation for any item that needs to be searched in the active nodes. It 
	returns a node index at which the item (movie) exists else it responds "not found"
	
	* LookupListItem() - this function is responsible to check if an item - movie name exists with an active node in the ring. If the movie exists at the node, 
	then it returns the item detail (movie details) else it responds with "not found" message. 
	
	* InsertItem() - this function is responsible to insert an item (movie) and its value (movie details) to the node at which it must be present.
	
	* DeleteNodeInRing() - this function is responsible to deactivate a node that is when a node is leaving the ring along with notifying every other node. 
	This makes the node as inactive in the cloud ring, notifies its successor and predecessor to update their values, along with transfers all its keys (items)
	to its successor and update the finger table of all nodes active in the system. If this is the only node active in the ring then the ring is deactivated
	and all the items are deleted from this node. Now there will be no active nodes in the ring and inserting a node will start from creating a ring.
	
	* DeleteKey() - this function is responsible to delete an item (movie) if present at any node. The lookup for the item is already performed and if the 
	response returns an active node, then deletion of key is performed for that node.
	
	* SnapshotActors() - this function is responsible to take snapshots of the system which is stored in ** ChordSnapshot.txt **. The snapshots contains
	the active nodes, their successor, predecessor, their list of items and their finger table.
	
	### Actors ###
	
	* ChordMainActor - this is the main actor for instantiating the actors for the chord ring. The different cases in this actor are called from the
	different definitions specified in chordMainMethod.
	
	* CloudNodeActor - this is the actor represnting each node in the ring. Each actor has its own successor, predecessor, finger table and list of items 
	it will be storing. The important definitions in this actor are:
	
		1. transferKeys() - this function is responsible to transfer keys to the successor node when a node leaves.
		
		2. nodeLeaveUpdateSucc() - this function is responsible to update the leaving nodes successor to update its predecessor node.
		
		3. nodeLeaveUpdatePred() - this function is responsible to update the leaving nodes predecessor to update its successor node.
		
		4. locate_successor() - this function is responsible to iterate through the rows of finger table of the current node and update 
		their successor nodes column.
		
		5. find_successor() - this function is responsible to find the successor for the current node.
		
		6. find_predecessor() - this function is responsible to find the predecessor for the current node.
		
		7. closest_preceding_finger() - this function is responsible to find the closes preceding nodes for a specified node.
		
		8. Stabilize() - this function is responsible to update the successor and predecessor for a node. This function is called when a new node joins the ring. This helps in updating the successor and predecessor for all nodes active in the ring. This internally calls notifynode which also along with updating the predecessor transfers the key from the node if required.



**2. mainHandlerService.scala** 
                                        
           Service Class =====> Request recieved =====>  Interacts with Chord Actor System to get response =====> Outputs response to rest call
          (starts web service,                                                                                                
          waits for rest calls)            
         

The webservice listens for REST calls to add/delete/lookup movie items, as well as add/remove nodes.


Once a request is received, the request if forwarded to the chord actor system, which provides a response which can be given as output to the rest call.

Addtionally, the web service also take a request to get a snapshot(localhost:8080/getSnapshot) of the entire node system. The entire snapshot is given as response to the rest call, as well as written to a file.


**3. User actor classes.scala**

<Description goes here (Abhijay)>


---------------------------------------------------------------------------------
			
**Scala test classes** (/src/test/scala/):

**chordMainMethodTest**

This file contains the unit test cases for testing the chord algorithm different functions:

   1. testCreateRingWithNode : this unit test case is responsible to test the creation of the ring with one node.
	
   2. testInsertNodeInRing : this unit test case is responsible to test inserting a new node into an existing active ring containing one node. This test is dependent on "testCreateRingWithNode".
	
   3. testDeleteNodeInRing : this unit test case is responsible to test the leaving of an existing node from the ring. This test is dependent on "testCreateRingWithNode".
	
   4. testLookupAndPut : this unit test case is responsible to test the lookup for an item (movie) in the dataset of nodes active in the ring. If the item is not found, then the item is added to the dataset of the node as returned from the lookup. This tests both lookup and insertion of an item.
	
   5. testDeleteKey : this unit test case is responsible to test the deletion of an existing item(movie) from the dataset of a node. This test case depends on "testLookupAndPut" and deletes the item(movie) from the nodes dataset.
	

**chordIntegrationTest.scala**
(Integration Testing for the entire system)

This integration test is to perform integration test of chord algorithm and the web service. The integration test instantiates the main actor and then 
calls the web service to create ring with a single node and insert an item into the active node in the ring.
	
**serviceTest.scala**: This testcase initiates the web service and makes rest calls to the web service and check its response. 

If the response matches, these test cases pass.


**Note:** While running the scala test programs for the first time, IntelliJ might show the error, "Module not defined". You can go to Run->Edit Configurations->Use classpath and SDK of module and specify the module there. And then rerun the test program.

**Note:** Sometimes IntelliJ automatically changes the test library for running the test cases. That might cause syntactical errors in our test programs. You can respecify the test library by removing the scala test library specified in build.sbt, and then putting it back again. 

The following scalatest library has been used:

libraryDependencies += "org.scalatest"  %% "scalatest"   % "2.2.4" % Test 

-------------------------------------------------------------------------------------------------------

**How to run the project:**

**OPTION 1(Run everything locally):**

1. Clone the repo and import the project into IntelliJ using SBT.

2. Edit the Run Configurations to add the Command Line Parameters:

These are in the order: noOfUsers totalNodes minRequests maxRequests simulationDuration snapshotMark moviefilePath readWriteRatio

For example:
5 8 0 20 5 2 "movies.txt" "4:1"

3. Run chordMainMethod

This does everything from starting the chord system, web service, and actor system, along with all the actor and snapshot simulations

**Note:** While running the scala programs for the first time, IntelliJ might show the error, "Module not defined". You can go to 

Run->Edit Configurations->Use classpath of module and specify the module there. And then rerun the program.

**OPTION 2(Run web service on the cloud):**

1. Copy build.sbt, /movies.txt and /src/main/ to a folder in your google cloud VM. 

           Run using SBT(From within the folder): sbt compile
   
           sbt "run <all command line parameters>"
		   
		   For example, sbt "run 5 8 0 20 10 2 movies.txt 4:1"

## After the web service is created, the URL to access it is http://localhost:8080 (if web service is run locally OR use your google cloud external IP)

   **Different rest calls that can be made to the webservice**
   Note: If simply clicking the URL doesn't work, copy it and paste in your browser.
	  
		http://127.0.0.1:8080/?insertNode=0
		
		http://127.0.0.1:8080/?nodeLeave=0
		
		http://127.0.0.1:8080/?getMovie=moviename
		
		http://127.0.0.1:8080/?putMovie=moviename&movieDetails=details
		
		http://127.0.0.1:8080/?deleteMovie=moviename

		Get the live Snapshot of the simulator: 

		http://127.0.0.1:8080/getSnapshot		
		
	
-------------------------------------------------------------------------------------------------------
**Bonus Implementations**

  1. Node joining: This allows a user to add a node in the ring. If there is no node in the ring, then the implementation calls ** CreateRingWithNode **  where the user can specify which node they want to add first in the ring. If the ring has atleast one active node, then the ** InsertNodeInRing** is called which adds the node to the ring on the basis of any active node in the ring. The implementation for these functions is present in ** ChordAlgorithm.scala ** =>  ** object chordMainMethod **. The unit test for ** CreateRingWithNode ** is : ** testCreateRingWithNode ** and for ** InsertNodeInRing ** is: ** testInsertNodeInRing **. Both the test cases are present in ** chordMainMethodTest.scala **. To run the test case, run testCreateRingWithNode and then testInsertNodeInRing. The paremeter passed for creating the ring or inserting the node in ring i.e. the node number can be changed to any number between 0 - 7. 

  2. Node leaving : This allows user to specify any node that they want to deactivate from the ring.  The method : ** DeleteNodeInRing ** is called to deactivate a node from the ring. This function is presnt in ** ChordAlgorithm.scala ** =>  ** object chordMainMethod **. The unit test case for node leaving is : ** testDeleteNodeInRing **. present in ** chordMainMethodTest.scala **. To run the test case, please run testCreateRingWithNode and then testDeleteNodeInRing. 

  3. Transfer of keys : This implementation allows to transfer the keys from one node to other node either when a node is leaving or if a new node is joining then the neighboring nodes can transfer existing keys from their dataset to the newly joined node. This implementation is present in function : ** transferKeys ** in ** ChordAlgorithm.scala ** => ** CloudNodeActor **. This function call happens when a node is leaving, then it will transfer all its keys to its successor. The unit test case : ** testDeleteNodeInRing **. present in ** chordMainMethodTest.scala ** performs this as an internal operation. When a new node joins : ** addKeys_whennodejoin ** - this operation is performed internally and if the keys needs to updated from one nodes dataset to other they will be transferred. The unit test case : ** testInsertNodeInRing ** in ** chordMainMethodTest.scala ** performs this operation internally.

  4. Delete Key : This implementation allows to delete an item (if existing) from a nodes dataset. This function is : ** DeleteKey ** present in ** ChordAlgorithm.scala ** =>  ** object chordMainMethod **. The unit test case is: ** testDeleteKey ** present in ** chordMainMethodTest.scala **. To run this test case, please run testLookupAndPut and then testDeleteKey.

  5. Snapshot at certain interval provided by user : this function allows us to output the current state of system that is the number of nodes added in the ring, each of the nodes successor, predecessor, their list items and their finger table. This function is ** SnapshotActors ** present in ** ChordAlgorithm.scala ** =>  ** object chordMainMethod **.

Two different logging framework: 

SLF4J has been used by the web service class to log input requests and their responses.

Dependency can be found in build.sbt and its implementation is in mainHandlerService.scala

ActorLogging was performed to check how much time it required to activate each node in the ring when create ring or insert node in ring are called. Different time logs are taken to check how much time is taken by different node actors to perform different operations.

Automated concurrent user request simulation which includes addition, deletion and retrieval of movies



**References:** present in "documents/references.txt"