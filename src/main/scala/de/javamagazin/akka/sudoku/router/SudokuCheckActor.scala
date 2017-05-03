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

import akka.actor.Actor
import de.javamagazin.akka.sudoku.msg.{CheckSudoku, CheckSudokuResponse}
import de.javamagazin.akka.sudoku.solver.Sudokus

/**
  * A child actor that checks sudoku definitions.
  */
class SudokuCheckActor extends Actor {
  override def receive: Receive = {
    case CheckSudoku(sudokuDef) =>
      sender ! CheckSudokuResponse(sudokuDef, Sudokus.checkSudoku(sudokuDef))
  }
}
