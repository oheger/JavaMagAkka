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

package de.javamagazin.akka.sudoku.request

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import de.javamagazin.akka.sudoku.msg.{SolveSudoku, SolveSudokuResponse}
import de.javamagazin.akka.sudoku.solver.{SudokuDef, Sudokus}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * A main class for invoking [[SimpleSudokuSolveActor]] to solve some sudokus.
  */
object SudokuSolveMain {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("SudokuSolveActor")
    implicit val timeout = Timeout(5.seconds)
    val actor = system.actorOf(Props[SimpleSudokuSolveActor], "sudokuSolver")

    def callActor(sd: SudokuDef): SolveSudokuResponse = {
      val futResp = actor ? SolveSudoku(sd)
      Await.result(futResp.mapTo[SolveSudokuResponse], 5.seconds)
    }

    val sd1 = Sudokus.sudoku(
      "0,0,0,0,2,0,7,0,9",
      "1,0,6,0,7,0,0,0,0",
      "9,5,0,3,0,0,0,0,0",
      "4,9,0,0,0,6,0,3,0",
      "0,0,0,4,0,7,0,0,0",
      "0,6,0,2,0,0,0,1,4",
      "0,0,0,0,0,2,0,8,6",
      "0,0,0,0,5,0,4,0,3",
      "6,0,8,0,4,0,0,0,0").get
    val sd2 = Sudokus.sudoku(
      "0,0,0,0,2,0,7,0,9",
      "1,0,6,0,7,0,0,0,0",
      "9,5,0,3,0,0,0,0,0",
      "4,9,0,0,0,6,0,3,0",
      "0,0,0,4,0,7,0,0,0",
      "0,6,0,2,0,0,0,1,4",
      "0,0,0,0,0,2,0,8,6",
      "0,0,0,0,5,0,4,0,3",
      "6,0,8,2,4,0,0,0,0").get
    println(callActor(sd1))
    println(callActor(sd2))

    val futTerm = system.terminate()
    Await.ready(futTerm, 5.seconds)
  }

}
