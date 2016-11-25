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

<Description goes here(Kruti)>



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

**chordIntegrationTest.scala**
(Integration Testing for the entire system)

<Description goes here>
	
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

<To receive this additional bonus, you need to implement and to describe how you will have implemented these additional features,
 where in the code they are located, and how to run tests to verify these functionalities.>

Node joining: 

Node leaving

Transfer of keys

Snapshot at certain interval provided by user

Two different logging framework: 

SLF4J has been used by the web service class to log input requests and their responses.

Dependency can be found in build.sbt and its implementation is in mainHandlerService.scala

<Write about Akka actors>

Automated concurrent user request simulation which includes addition, deletion and retrieval of movies



**References:** present in "documents/references.txt"