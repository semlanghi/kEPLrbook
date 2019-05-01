# kEPLrbook



A Jupyter notebook buildt for experimenting with EPL. 
Experimental interactive environment, every notebook could be considered an experiment itself, so could be then exported and stored. 
The experiment is based on 3 primary elements

* Input: the events in input that are inserted into the runtime
* Query: the query that is used to process the input 
* Expected Output (optional): the expected output that is eventually compared with the actual output

Mirroring this classification the system is based on a 4-tier architecture in which all the backend servers are connected through web sockets.

* Input Manager
* Output Manager
* Runtime Server
* Jupyter Server

## Input Manager

An input server that manages the input coming from the front-end of the application. The input is represented in YAML format and mapped using Jackson library ([Github](https://github.com/FasterXML/jackson)).

### Input Insertion

  _The input is sent to the server in YAML format and then processed. In the input the events are represented as a time keyed map in which are listed all the events that should be sent in that timestamp ([Sample](#body)). The server reads it, mapping it into a special list object called **EventList** and separates the events into single, timestampend entities, using a wrapper called **TimestampedEvent**._

* **URL**

  /input

* **Method:**
  
  `POST` 

* **<a name="body"> 
Data Params
</a>**

```
  inputs:
     0 :
       - type: "NbaGame"
         id: 1
         awayTeam: "Sacramento Kings"
         homeTeam: "New York Knicks" 
     1000 :
       - type: "NbaGame"
         id: 2
         awayTeam: "Sacramento Kings"
         homeTeam: "New York Knicks"

       - type: "NbaGame"
         id: 3
         awayTeam: "Sacramento Kings"
         homeTeam: "New York Knicks
```


* **Success Response:**
  
  * **Code:** 200
    **Content:** `Input file created.`
 
* **Sample Call:**

```
def sendMessage(self, content):
  r = self.session.post(self.host+"/input", content)
  print(r.text)
```
* **Notes:**

  * _The server store the input received as a file in order to maintain the last input._ 
  * _The serialization format of event sent to the runtime is JSON._
  * _After having sent all the events as TimestampedEvents the server send a "finish" message in order to indicate that there are no more input events._

## Runtime Server

The server on which is located the Esper Runtime. It receives through web socket the various input events and sends them into the Esper Runtime.

### Query Insertion

_The server exposes a single RESTful endpoint that is used to post the EPL module used to process the various events._

* **URL**

  /query

* **Method:**
  
  `POST` 

* **Data Params**

```
create schema NbaGame as (type string, id int, awayTeam string, homeTeam string); 
@name('Output')select sum(id) from NbaGame#time(500 milliseconds) output every 500 milliseconds; 
```

* **Success Response:**
  
  * **Code:** 200
    **Content:** `Query file created.`
 
* **Sample Call:**

```
def sendMessage(self, content):
  r = self.session.post(self.host+"/query", content)
  print(r.text)
```
* **Notes:**

  * _The server store the module received as a file in order to always maintain and use at least a stored module in order to process the input._ 
  * _The module **is not immediately deployed** but is simply stored._
  * _The module is deployed into the runtime by the web socket manager object when the first event arrives and the module file has been modified. In that case all the previous modules are undeployed and the new module is loaded._
  

## Output Manager

The Output Manager receives the resulting events from the runtime through the web socket. If an expected output is specified, this is received from the server and used to compare it with the actual output. The server receives the event **one by one** so basically it collects them and, once the "finished" message arrives, stores the event onto a file, using it after for comparison if the expected output is present. 

_In case the expected output is not specified the Output Manager will simply compose a representation of the actual output._

### Output Insertion

  _The expected is sent to the server in YAML format and then processed. In the output the events are represented as a time keyed map in which are listed all the events that should be arrived in that timestamp ([Sample](#output)), just like the input. The server reads it, mapping it into the object **EventList**._

* **URL**

  /output

* **Method:**
  
  `POST` 

* **<a name="output"> 
Data Params
</a>**

```
  inputs:
     0 :
       - type: "NbaGame"
         id: 1
         awayTeam: "Sacramento Kings"
         homeTeam: "New York Knicks" 
     1000 :
       - type: "NbaGame"
         id: 2
         awayTeam: "Sacramento Kings"
         homeTeam: "New York Knicks"

       - type: "NbaGame"
         id: 3
         awayTeam: "Sacramento Kings"
         homeTeam: "New York Knicks
```


* **Success Response:**
  
  * **Code:** 200
    **Content:** `Created expected output file.`
 
* **Sample Call:**

```
def sendMessage(self, content):
  r = self.session.post(self.host+"/output", content)
  print(r.text)
```
* **Notes:**

  * _The server store the expected output received as a file in order to maintain the last expected output._ 
  * _The serialization format of event received by the runtime is JSON._
  

### Output Link Retrieval

  _At first the server checks that both the "actual output" and the "expected output" files exists, then if in the "expected output" file is present the server will proceed to writing the "comparison.txt" file. The comparison is done checking both the **EventList** objects (composed from the previously cited output files, using the jackson mapper), checking if both elements have at first the same timestamps, then comparing the event lists for each timestamp. At the end the method will return the **link to retrieve the comparison result**. If no expected output is provided (keyword "gotcha" in the "expected output" file) the comparison file will be written with the content of the "actual output" file_
  

* **URL**

  /output

* **Method:**
  
  `GET` 

* **Success Response:**
  
  * **Code:** 200
    **Content:** `http://localhost:1234/final_output`
 
* **Sample Call:**

```
def getRequest(self):
  r = self.session.request('GET', self.host+'/output')
  print(r.text)
```
* **Notes:**

  * _The sources of comparison **are the files**, this is due to the fact that they provide a possibility for monitoring, in fact if there is a problem with the parsing and mapping of the expected output (and also of the input in the Input Manager) i can check directly on the file the source of the problem, this is also valid because it gives an intermediate directly consultable representation of the actual output_
  * _The comparison file is a txt file since gives more freedom on the format of representation, since it must adapt to a comparison result (a simple text), and also to the actual output representation (text written in YAML format)_
  

### Output Retrieval

_Endpoint for the retrieval of the final result, being it the actual output or the comparison result. The endpoint is reached when, through the browser, the result link is clicked, this way the server can set up the response of a get request from the browser with the final result._


* **URL**

  /final_output

* **Method:**
  
  `GET` 

* **Success Response:**
  
  * **Code:** 200
    **Content:** 
      
      * Actual Output
      ```
      ---
      inputs:
        0:
        - sum(id): 1
        1000:
        - sum(id): 3
        - sum(id): 6
      ```
      
      * Comparison Result
      
     ```
     Start Comparison:
      Time 0:
       - 0th events: 
        - Actual event : 
          {sum(id)=1}
        - Expected event: 
          {type=NbaGamer, id=1, awayTeam=Sacramento Kings, homeTeam=New York Knicks}
        - Events are different
      ******************************
      Time 1000:
       - 0th events: 
        - Actual event : 
          {sum(id)=3}
        - Expected event: 
          {type=NbaGamer, id=2, awayTeam=Sacramento Kings, homeTeam=New York Knicks}
        - Events are different
       - 1th events: 
        - Actual event : 
          {sum(id)=6}
        - Expected event: 
          {type=NbaGamer, id=3, awayTeam=Sacramento Kings, homeTeam=New York Knicks}
        - Events are different
      ******************************
     ```

 
* **Sample Call:**

```
def getRequest(self):
  r = self.session.request('GET', self.host+'/final_output')
  print(r.text)
```


