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

```inputs:
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

_The server exposes a single RESTful endpoint that is used to post the EPL module used to process the various events.
