# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoFmipzGsvz-BwVygYKQH+uB5afJCIJqTsXzQo8wHiVQSIwAgQnihignCQSRJgKSb6GLuNL7gyTJTspXI3l5d5LsKMBihKboesODpJr6zpdhu7pbpgKrBhqACSaBUCaSDrsZALbh5ln+sy0yXtASAAF4oBwUYxnGhRabF8DIKmMDpgAjAROaqHm8yLJl2XIHl+h-CZdFNoVgrRfSMCHnIKDPvEGJLdeM0CiF9SPgGl6vvF7Y6aWjnihkNgFAYmCHSB1S6YR+n9TB5GXlR9aQppN3wsmrXYTAuH4aMd3vJBhlPfBiGvbRjYMYx3h+P4XgoOgMRxIkCNI45vhYKJgqgfUDTSBG-ERu0EbdD0cmqApwwUS9jUfdp5m3TT4PIW9l2M-CcWdvUtn2Jj57PSzbn7Z5vIjvUQ25QtS0C2DSFzkFMWVMuYXik+u3Jal6owINOXrpASFTc1uOSmVeoVdVtXRigsYKehn2VKJaZOF1AM9X1ey68NxqXjABv1hN0PudNt78vUHAoNwx6XrLlFC4FYtK0K9QZDMEA0DtL5bsHzWHfUZA+Pup3YOdYhXcbH0vO9EkOy1JRgDheFZoHpieLDAQouu-jYOKGr8WiMAAOJKho2PFaWDSD8TZP2Eq1OC4bTWVHnMDjMz1Fs+XXNWS6NlosPOax7TwudiHiuzZLWjR2e6-oArieLsroXherWfyKYWsZVlet+yzRvj66M28QLY1TqjbBq9tMLfQbu1F23V+QewGt-b2FFf7URbkbKkodZqMjAIGBAB81AYnvguDaT96gvyHkqHkDROj-2Xhzeo-cciENUMXUu7NYQYQrjXF4YxZ45gWA0FwIjOjV0TF9eujd-rjAEWoIRIiXBiKhq3JicMOAAHY3BOBQE4GIEZghwC4gANngBOQwhCYBFGgTjSujRWgdBnnPIBtMsxyIAHJKnEdwhhXCwL8KVJ4gykMt4dh3glea6JCEYjgOYwhzk1CuUwTIbBo4YCX2ljHW+aASH7nvM-NWmcrya1VF-S+aD0D0LCSVIBICrb1Ttk1SRbVOrwNzPmJB5TUH+2QhgnOWDz5pMiSgaJhC1qpPyVtNcVC5h7VPrnRhZijwjKVOwtAF1y4ANXgEuY6VpCmUgc1J2v0m4AzkXsg59FVHt38JYSOtlNjIyQAkMAdy+wQEeQAKQgOKGZhh-DJFAGqax9dbG8PscyGSPQ5HzzlugLM2AEDADuVAOAEBbJQDWOc6Q3jOYPD8XwxFyLKBooxV8EE2KDmhPcvUAAVj8tA0TvniniSgQkiST7hLPg-NJGTr7LWybk4K5DVYSlWiUtKOtkG5QqXTeZWzSoUTqWA228YmmO2gc7V22YEEdLGF7GV3SWYNiuck9aPkwDROxUKpOKtKFjIldrSxaAIDMAAGa+CbDnLZ7jXVhU9euFVED1V1xaU4QIbTep6udX6j1PhOAmsmv0lJgz6h+CvisuYTQkUotUBibFaw3hrBAOi6ANrH7J0lNgDNfy5lcoWQS8ghcRxrI2Rza64KRi4qgVIk5MiMFt2YvDKAyKnkvK8KOxAwZYDAGwIiwgeQChWLHtvSSBMiYkzJsYQ5vivz1HXcTCMvRt3UvivUEA3A8DELNak89l6oBujzeWshlbpCRyZIYSK2cRYDJ5Xe6dMtn2TJkO+9ERS62Dl-aQ-9eBWHXvWsBt9UcLHUO-fK3dcJ87Nv5K2su7aeE1Croc5pP0-rNxUZgIAA)



## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
