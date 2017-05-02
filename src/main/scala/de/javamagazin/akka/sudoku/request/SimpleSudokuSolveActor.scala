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

import akka.actor.Actor
import de.javamagazin.akka.sudoku.msg.{SolveSudoku, SolveSudokuResponse}
import de.javamagazin.akka.sudoku.solver.Sudokus
import de.javamagazin.akka.sudoku.solver.Sudokus.SudokuException

/**
  * An actor class that processes requests to solve sudokus.
  */
class SimpleSudokuSolveActor extends Actor {
  override def receive: Receive = {
    case SolveSudoku(sudokuDef) =>
      Sudokus.checkSudoku(sudokuDef) match {
        case Left(error) =>
          sender ! SolveSudokuResponse(sudokuDef, Some(new SudokuException(error)), 0)

        case Right(tables) =>
          val startTime = System.nanoTime()
          val solvedSudoku = Sudokus.solve(sudokuDef, tables)
          sender ! SolveSudokuResponse(solvedSudoku, None, System.nanoTime() - startTime)
      }
  }
}
