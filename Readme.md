# Matchmaking
This is a simple matchmaking server that can be used for any game.

**NOTE: This software is still under development and some of the features below might not be implemented yet, so hang tight and watch this repository to get news :)**

## Features
1. Can be used for any game
2. The game can be written in any programming language (doesn't have to be Java or Kotlin)
3. HTTP interface
4. JSON api
5. Create rooms of any size
6. Global white-/blacklist support
7. Per-room white-/blacklist support
8. Games can be resumed even after a client looses the connection
9. Scalable: Can be launched with a single command on one single node or distributed over a large network of nodes

## Download
Hang on, development is still in progress ;) Therefore:

1. Cloe this repo
2. Run `mvn package`

## Running the server
The server is built as a Java-EE-webapp. You can therefore run it on any Servlet-Container of your liking (Tomcat, Jetty, ...) or deploy it to PaaS-providers like Heroku.

For the sake of simplicity, you can also use [jsimone's webapp-runner](https://github.com/jsimone/webapp-runner). To do so, clone and build the project as explained above and then run the following command:
```
java -jar server/target/dependency/webapp-runner.jar server/target/matchmaking.server-1.0-SNAPSHOT.war --PORT 8080
```

## How it works
### ... in general
- Clients connect themselves to the server and tell the server that they wish to connect to a room.
- If the client is the first to connect, a new room will be created and the client will have to wait for the room to become full.
- Clients who connect afterwards will be assigned to the same room until the room is full and a game can begin.
- Once the game has started, the server acts as a relay station. Clients send updates to the server and the server distributes the current state of the game to all clients. This technique circumvents the need for peer-to-peer connections between the players.
- If a client looses its connection to the server but manages to reconnect eventually, the server will recognize the client and the game can resume

### Java/Kotlin specifics
Good news! We will be providing a client for you (once we finished development, obviously ;) ), so you can focus on your game. Again, it's not yet finished, so hang tight :)

### Other languages
If your language is not a JVM-language, you will have to write your own client and implement the communication protocol yourself.
Consider submitting your implementation through a pull-request if you wish to share it with the public.