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

package de.javamagazin.akka.sudoku.msg

import akka.actor.ActorRef
import de.javamagazin.akka.sudoku.solver.SudokuDef
import de.javamagazin.akka.sudoku.solver.Sudokus.CheckError

/**
  * A message class requesting a check of a sudoku definition.
  *
  * @param sudokuDef the definition to be checked
  * @param client    the requesting client actor
  */
case class CheckSudoku(sudokuDef: SudokuDef, client: ActorRef)

/**
  * A message requesting that a sudoku is solved.
  *
  * @param sudokuDef the definition to be solved
  */
case class SolveSudoku(sudokuDef: SudokuDef)

/**
  * A message sent as response of a [[SolveSudoku]] request.
  *
  * The message contains the solved sudoku if the operation was successful.
  * Otherwise, an error message is provided with information why a
  * verification failed.
  *
  * @param sudokuDef  the definition for the solved sudoku
  * @param checkError an option for an error of a check operation
  * @param time       the time for solving the sudoku
  */
case class SolveSudokuResponse(sudokuDef: SudokuDef, checkError: Option[CheckError],
                               time: Long) {
  /**
    * Returns a flag whether this operation was successful, and the sudoku
    * could be solved.
    *
    * @return '''true''' if the sudoku was solved; '''false''' otherwise
    */
  def isSuccess: Boolean = checkError.isEmpty && sudokuDef.solved
}
