# JavaMagAkka
This repository contains example code for an article in the German
[Java Magazin](https://jaxenter.de/magazine/java-magazin) about an introduction into
the [Akka Framework](http://akka.io/). The article uses solving
[sudokus](https://en.wikipedia.org/wiki/Sudoku) as example domain.
 
The packages cover different examples. The following ones are available:
* _hello_ contains a simple Hello World actor to show the basic anatomy of an actor.
* The packages below _sudoku_ contain examples based on solving sudokus. Here the following sub packages are provided:
* _solver_ contains the classes used to define sudokus and to solve them. Here a simple back-tracking algorithm is implemented.
* _msg_ defines common messages used for the interaction with example actors; some messages are shared between examples.
* _request_ is an example of an actor that is invoked using the _ask_ pattern and returns an answer. It can solve a sudoku and returns the solution.
* _router_ contains an example for solving sudokus in parallel. There is a service
  actor which is called with requests to solve sudokus. It then delegates these
  requests to child router actors that handle verification and solving of sudokus.
  The package also demonstrates error handling and the Akka death watch feature.
