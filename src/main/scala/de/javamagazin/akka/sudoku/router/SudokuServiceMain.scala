/*
 * Copyright 2017 Oliver Heger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.javamagazin.akka.sudoku.router

import java.util.concurrent.CountDownLatch

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import de.javamagazin.akka.sudoku.msg.{SolveSudoku, StatsWritten, WriteStats, WriteStatsFailed}
import de.javamagazin.akka.sudoku.solver.Sudokus

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Main class for calling and benchmarking the [[SudokuServiceActor]] class.
  *
  * The service actor is able to solve sudokus in parallel by delegating to
  * child actors.
  *
  * This test driver defines a number of input sudoku definitions. Based on
  * these a number of requests is generated and sent to the service actor. The
  * time is measured until all answers have been received.
  */
object SudokuServiceMain {
  private val SudokuDefinitions = Array(
    Sudokus.sudoku("7,5,0,9,0,0,0,0,3",
      "0,0,1,3,0,0,9,0,4",
      "0,4,0,5,6,8,0,2,0",
      "0,0,4,0,0,0,8,7,1",
      "0,0,3,0,1,0,5,0,0",
      "1,6,8,0,0,0,4,0,0",
      "0,2,0,1,9,7,0,4,0",
      "4,0,7,0,0,5,2,0,0",
      "9,0,0,0,0,3,0,6,8").get,
    Sudokus.sudoku("0,8,0,0,0,9,0,2,1",
      "4,0,0,8,0,0,3,6,0",
      "0,1,0,7,3,0,0,0,0",
      "6,7,3,0,0,0,5,4,0",
      "0,0,2,6,0,5,8,0,0",
      "0,4,5,0,0,0,2,1,6",
      "0,0,0,0,5,8,0,9,0",
      "0,3,1,0,0,7,0,0,4",
      "9,2,0,4,0,0,0,3,0").get,
    Sudokus.sudoku("5,4,0,6,7,0,0,1,0",
      "0,0,3,2,0,8,0,0,0",
      "0,0,0,0,0,0,0,0,3",
      "0,2,0,4,0,0,1,0,0",
      "0,0,4,0,0,0,6,0,0",
      "0,0,6,0,0,9,0,8,0",
      "3,0,0,0,0,0,0,0,0",
      "0,0,0,8,0,7,3,0,0",
      "0,0,0,0,9,6,0,2,4").get,
    Sudokus.sudoku("9,0,0,3,0,0,0,8,1",
      "3,7,0,0,0,5,0,0,0",
      "0,0,0,2,7,0,0,0,0",
      "8,3,0,0,0,0,2,0,0",
      "0,0,6,0,0,0,9,0,0",
      "0,0,2,0,0,0,0,7,5",
      "0,0,0,0,6,8,0,0,0",
      "0,0,0,4,0,0,0,2,3",
      "7,4,0,0,0,2,0,0,9").get
  )

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("SudokuSolveService")
    implicit val timeout = Timeout(10.seconds)
    implicit val ec = system.dispatcher
    val actor = system.actorOf(Props[SudokuServiceActor], "sudokuService")
    val Count = 10000
    val latch = new CountDownLatch(Count)
    val stream = Stream.from(1)
    val startTime = System.nanoTime()

    stream.take(Count).foreach { i =>
      val futSolve = actor ? SolveSudoku(SudokuDefinitions(i % SudokuDefinitions.length))
      futSolve.andThen {
        case Success(_) =>
          latch.countDown()
        case Failure(exception) =>
          exception.printStackTrace()
          latch.countDown()
      }
    }

    latch.await()
    if (args.length > 0) {
      val futWrite = actor ? WriteStats(args.head)
      Await.result(futWrite, 5.seconds) match {
        case StatsWritten =>
          println("Statistical data written to " + args.head)
        case WriteStatsFailed =>
          println("Could not write statistical data to " + args.head)
      }
    }
    println(s"Solved $Count sudokus in ${System.nanoTime() - startTime} ns.")
    val futTerm = system.terminate()
    Await.ready(futTerm, 5.seconds)
  }
}
