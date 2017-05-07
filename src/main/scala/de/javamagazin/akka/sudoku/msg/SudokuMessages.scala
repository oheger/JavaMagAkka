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

import de.javamagazin.akka.sudoku.solver.Sudokus.CheckError
import de.javamagazin.akka.sudoku.solver.{LookupTables, SudokuDef}

/**
  * A message class requesting a check of a sudoku definition.
  *
  * @param sudokuDef the definition to be checked
  */
case class CheckSudoku(sudokuDef: SudokuDef)

/**
  * A message class representing the result of a sudoku check operation.
  *
  * @param sudokuDef   the definition that has been checked
  * @param checkResult an object with the check result (either successful or
  *                    failed)
  */
case class CheckSudokuResponse(sudokuDef: SudokuDef, checkResult: Either[CheckError, LookupTables])

/**
  * A message requesting that a sudoku is solved.
  *
  * @param sudokuDef the definition to be solved
  */
case class SolveSudoku(sudokuDef: SudokuDef)

/**
  * A message requesting that a sudoku which has been checked successfully is
  * solved. The message contains a ''LookupTables'' object which is needed to
  * solve the sudoku efficiently.
  *
  * @param sudokuDef the definition to be solved
  * @param tables    the lookup tables for the solve operation
  */
case class SolveCheckedSudoku(sudokuDef: SudokuDef, tables: LookupTables)

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
case class SolveSudokuResponse(sudokuDef: SudokuDef, checkError: Option[Throwable],
                               time: Long) {
  /**
    * Returns a flag whether this operation was successful, and the sudoku
    * could be solved.
    *
    * @return '''true''' if the sudoku was solved; '''false''' otherwise
    */
  def isSuccess: Boolean = checkError.isEmpty && sudokuDef.solved
}

/**
  * A message requesting that a file with statistical information is written to
  * the specified path.
  *
  * @param path the path
  */
case class WriteStats(path: String)

/**
  * A message indicating that a file with statistics data has been written
  * successfully.
  */
case object StatsWritten

/**
  * A message indicating that a file with statistics data could not be written.
  */
case object WriteStatsFailed
