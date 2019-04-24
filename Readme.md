# Matchmaking
This is a simple matchmaking server that can be used for any game.

## Motivation
Developing an online multiplayer game is hard. Not only do you need to develop your game, you also need to think about 
networking, create a communication protocol and develop server software. "Matchmaking" does all of the multiplayer-related 
aspects for you, all you need to do is to create your game. On the other hand, "Matchmaking" doesn't do anything else:
It's not a game engine and gives you complete freedom in how you create your game.

## Features
1. Can be used for any game (*)
2. Easy configuration through a web interface, command line parameters or configuration files [WIP]
3. JSON api (over HTTP)
4. Create rooms of any size
5. Global white-/blacklist support
6. Per-room white-/blacklist support
7. Games can be resumed even after a client looses the connection
8. Scalable: Can be launched with a single command on one single node or distributed over a large network of nodes [WIP]

(*): You can program your game in any programming language. Furthermore, you can use any game engine that you want to 
create your game or write your game from scratch. The only thing that your game needs to have is the ability to 
communicate via HTTP and work with JSON.

### Project maturity
This project is far from being finished, but it is already in a usable state. You can therefore go ahead and download 
the server software to start experimenting with it, but keep the following in mind:

- You are using a snapshot build. This means that things can change from one moment to another without prior notice.
- The server can currently only run on one single node as I am still working on database integrations.
- For the same reason, data is currently not persisted across server restarts.
- You currently have to stick with the default configuration as I am still working on the configuration utilities.
- You will have to write your own client as I didn't have the time yet to create one. (You are always welcome to contribute your client through a pull request)

## Download
Development is still in progress and precompiled artifacts are not yet available (but will be available soon, though).
You therefore need to build the server yourself as described below in the section "Building from source".

## Running the server
### Prerequisites
- JRE 8 or above (though only tested with JRE 8)

### Standalone server launcher
"Matchmaking" comes with a standalone launcher which uses Tomcat 8 as its basis. The standalone launcher can simply be launched with tis command:
```
java -jar matchmaking.standalone-server-launcher-1.0-SNAPSHOT-jar-with-dependencies.jar --PORT 8080
```

Replace `8080` with the port that you want the server to listen on.

### Running in a Servlet container
You can use any Java Servlet container like Tomcat or Jetty to run the war file or deploy it to PaaS-providers like Heroku.
To do so, please follow your container's instructions on how to deploy a `war` file and deploy the file called `matchmaking.server-1.0-SNAPSHOT.war`.

### Docker image
There will be a docker image which you can use to run the server, but that is a huge work in progress.

### Verifying that your server is running
Open your browser and navigate to the URL of your server (`http://localhost:8080` if you're running the server on your machine).
You should be greeted by a welcome message.

## Building from source
Building the server from source is actually quite simple and only requires the following to be installed:

- Java JDK 1.8
- Maven 3 or higher

To build the software from source, do the following

1. Clone or download the repository (Use the big green button in the upper right corner). If you downloaded the repository, unzip it, before continuing.
2. Open your command line and use `cd` to navigate into the project directory
3. Type `mvn package`, hit enter and watch the magic happen (Use `mvn package -DskipTests=true` to skip the unit tests)

Once the build has finished, you will find the compiled artifacts at the following places:

- The server war: `server/target/matchmaking.server-1.0-SNAPSHOT.war`
- The standalone server launcher: `standaloneserverlauncher/target/matchmaking.standalone-server-launcher-1.0-SNAPSHOT-jar-with-dependencies.jar`

## How it works
### ... in general
- Clients connect themselves to the server and tell the server that they wish to connect to a room.
- If the client is the first to connect, a new room will be created and the client will have to wait for the room to become full.
- Clients who connect afterwards will be assigned to the same room until the room is full and a game can begin.
- Once the game has started, the server acts as a relay station. Clients send updates to the server and the server distributes the current state of the game to all clients. This technique circumvents the need for peer-to-peer connections between the players.
- One client will become the so-called host of the game. This client verifies that all other clients play within the rules and tells the server if there is something off
- If a client looses its connection to the server but manages to reconnect eventually, the server will recognize the client and the game can resume

### Java/Kotlin specifics
Good news! We will be providing a client for you (once we finished development, obviously ;) ), so you can focus on your game. Again, it's not yet finished, so hang tight :)

### Other languages
If your language is not a JVM-language, you will have to write your own client and implement the communication protocol yourself.
Consider submitting your implementation through a pull-request if you wish to share it with the public.

## The communication protocol
### General things
#### API endpoint
All communication happens over HTTP through JSON-formatted requests. All requests must be sent to 
```
http://host:port/matchmaking
```
(or `https://host:port/matchmaking` if you use `https`)

#### HTTP headers
The server doesn't care too much about what headers you send, just two rules that you should follow:
- Set `Content-Length` to the correct value or omit it if you don't know the length of the request
- Set `Content-Type` to `application/json`

#### HTTP body and general request format
The actual request is sent in the HTTP request body and is formatted as JSON. 
Requests always follow this pattern:

```json
{
  "className": "<requestType>", 
  "additionalParameterName": "parameterValue"
}
```

The `className` property specifies the type of the request. 
The `className` of a request always starts with `com.github.vatbub.matchmaking.common.requests`

#### Server responses
The responses given by the server follow the same pattern as requests, except that the `className` starts with `com.github.vatbub.matchmaking.common.responses`.
In addition, the server uses HTTP status codes to indicate the nature of the response.

### Initializing a connection
During the communication, the client will have to send multiple requests to the server. Unfortunately, HTTP does not provide a reliable way for the server to recognize clients. 
Hence, the very first request that a client needs to send is a `GetConnectionIdRequest` which always looks like this:
```json
{
  "className": "com.github.vatbub.matchmaking.common.requests.GetConnectionIdRequest"
}
```

The server will then assign a `connectionId` and a `password` to the client and return those credentials using a `GetConnectionIdResponse` which will look like so:
```json
{
  "password": "3450e711",
  "httpStatusCode": 200,
  "connectionId": "79f96ee2",
  "className": "com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse"
}
```

The client is expected to remember the `connectionId` and the `password` for later communication.

### Creating or joining a room
Players who are playing together are grouped in rooms. There can be multiple rooms at the same time and each room is completely independent from other rooms. That means:

- Different rooms can be in different stages in the game
- Different rooms can have different game rules

Additional things to keep in mind:
- One Player (i. e. one `connectionId`) can only be in one room at a time

To create or join a room, the client needs to send a `JoinOrCreateRoomRequest` which looks like so:
```json
{
  "operation": "JoinOrCreateRoom",
  "userName": "vatbub",
  "userList": [
    "heykey",
    "mo-mar"
  ],
  "userListMode": "Whitelist",
  "minRoomSize": 1,
  "maxRoomSize": 2,
  "connectionId": "79f96ee2",
  "password": "3450e711",
  "className": "com.github.vatbub.matchmaking.common.requests.JoinOrCreateRoomRequest"
}
```

**Parameters:**

| parameter      | possible values                                                                  | explanation                                                                                                                                                                                                                                                                                                                            |
|:---------------|:---------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `operation`    | `JoinOrCreateRoom`, `JoinRoom` `CreateRoom`                                      | Specifies what operation shall be done. `JoinOrCreateRoom` will cause the player to join a room if an applicable room is found or create a new room if no matching room was found. `JoinRoom` will do the same except that nothing will happen if no applicable room can be found. `CreateRoom` will force the creation of a new room. |
| `userName`     | Any string value                                                                 | The human-readable user name that was picked by the user. Similar to the XBox Gamertag or PSN-ID                                                                                                                                                                                                                                       |
| `userList`     | Array of strings                                                                 | White- or blacklist of usernames                                                                                                                                                                                                                                                                                                       |
| `userListMode` | `Whitelist`, `Blacklist` or `Ignore`                                             | Specifies how `userList` shall be treated. `Whitelist`: Only users mentioned in `userList` will be allowed to join the room. `Blacklist`: Users mentioned in `userList` will not be allowed to join the room. `Ignore` (default): `userList` will have no effect.                                                                      |
| `minRoomSize`  | Any integer value above or equal to 0                                            | The minimum amount of players required to start a game.                                                                                                                                                                                                                                                                                |
| `maxRoomSize`  | Any integer value above or equal to 0. Should be above or equal to `minRoomSize` | The maximum amount of players allowed in a room.                                                                                                                                                                                                                                                                                       |
| `connectionId` |                                                                                  | The sending client's `connectionId` as assigned by the server in the `GetConnectionIdResponse`                                                                                                                                                                                                                                         |
| `password`     |                                                                                  | The sending client's `password` as assigned by the server in the `GetConnectionIdResponse`                                                                                                                                                                                                                                             |

The `JoinOrCreateRoomResponse` returned by the server will look like so:
```json
{
  "result": "RoomCreated",
  "roomId": "73065963",
  "httpStatusCode": 200,
  "connectionId": "79f96ee2",
  "className": "com.github.vatbub.matchmaking.common.responses.JoinOrCreateRoomResponse"
}
```

**Parameters:**

| parameter | possible values                       | explanation                                                                                                                                                       |
|:----------|:--------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `result`  | `RoomCreated`, `RoomJoined` `Nothing` | The operation that was performed on the server as a result of the request.                                                                                        |
| `roomId`  | String                                | The id of the room that was created or that the player joined. This id can later be used to retrieve information about the room or to modify the room's contents. |

### Retrieving information about a room
Once a room was created or joined, the client can request additional information about the room it was assigned to using a `GetRoomDataRequest`:
```json
{
  "roomId": "73065963",
  "connectionId": "79f96ee2",
  "password": "3450e711",
  "className": "com.github.vatbub.matchmaking.common.requests.GetRoomDataRequest"
}
```

**Parameters:**

| parameter      | possible values | explanation                                                                                    |
|:---------------|:----------------|:-----------------------------------------------------------------------------------------------|
| `roomId`       | String          | The `roomId` as specified by the `JoinOrCreateRoomResponse`                                    |
| `connectionId` |                 | The sending client's `connectionId` as assigned by the server in the `GetConnectionIdResponse` |
| `password`     |                 | The sending client's `password` as assigned by the server in the `GetConnectionIdResponse`     |

The 'GetRoomDataResponse` returned by the server will look like so:

```json
{
  "room": {
    "connectedUsers": [],
    "gameState": {
      "contents": {}
    },
    "gameStarted": false,
    "dataToBeSentToTheHost": [],
    "id": "73065963",
    "hostUserConnectionId": "79f96ee2",
    "configuredUserNameList": [
      "heykey",
      "mo-mar"
    ],
    "configuredUserNameListMode": "Whitelist",
    "minRoomSize": 1,
    "maxRoomSize": 2
  },
  "httpStatusCode": 200,
  "connectionId": "79f96ee2",
  "className": "com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse"
}
```

**Parameters:**

| parameter | possible values | explanation                                                                                                |
|:----------|:----------------|:-----------------------------------------------------------------------------------------------------------|
| `room`    | Room-object     | Object which contains information about the requested room. (See below for an explanation of Room-objects) |

