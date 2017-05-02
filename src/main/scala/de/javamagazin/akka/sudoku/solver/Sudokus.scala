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

package de.javamagazin.akka.sudoku.solver

import scala.annotation.tailrec
import scala.util.Try

/**
  * An object implementing functionality related to checking and solving
  * Sudokus.
  */
object Sudokus {

  /**
    * A trait indicating an error when checking a Sudoku definition.
    *
    * The methods provide information about the position of the error and the
    * value involved. Concrete implementations define different types of
    * errors.
    */
  sealed trait CheckError {
    /**
      * Returns the row index of the error.
      *
      * @return the row index
      */
    def rowIndex: Int

    /**
      * Returns the column index of the error.
      *
      * @return the column index
      */
    def columnIndex: Int

    /**
      * Returns the value of the cell with the error.
      *
      * @return the value
      */
    def value: Int
  }

  /**
    * A special error indicating that a Sudoku definition contains an invalid
    * number.
    *
    * @param rowIndex    the row index
    * @param columnIndex the column index
    * @param value       the value
    */
  case class ErrorInvalidNumber(override val rowIndex: Int, override val columnIndex: Int,
                                override val value: Int) extends CheckError

  /**
    * A special error indicating that a value contains twice in a row.
    *
    * @param rowIndex    the row index
    * @param columnIndex the column index
    * @param value       the value
    */
  case class ErrorRowDuplicate(override val rowIndex: Int, override val columnIndex: Int,
                               override val value: Int) extends CheckError

  /**
    * A special error indicating that a value contains twice in a column.
    *
    * @param rowIndex    the row index
    * @param columnIndex the column index
    * @param value       the value
    */
  case class ErrorColumnDuplicate(override val rowIndex: Int, override val columnIndex: Int,
                                  override val value: Int) extends CheckError

  /**
    * A special error indicating that a value contains twice in a square.
    *
    * @param rowIndex    the row index
    * @param columnIndex the column index
    * @param value       the value
    */
  case class ErrorSquareDuplicate(override val rowIndex: Int, override val columnIndex: Int,
                                  override val value: Int) extends CheckError

  /**
    * An exception class reporting errors when solving a sudoku caused by a
    * failed check operation.
    *
    * @param checkError the ''CheckError''
    */
  class SudokuException(val checkError: CheckError) extends Exception(checkError.toString)

  /**
    * Creates a ''SudokuDef'' from a string representation of the single rows.
    * Each row is a string with the numbers at their corresponding column
    * positions, separated by comma and optional whitespace.
    *
    * @param rows the rows of the sudoku
    * @return a ''Try'' with the parsed sudoku definition
    */
  def sudoku(rows: String*): Try[SudokuDef] =
    Try {
      val field = rows.map(_.split(",").map(_.trim.toInt))
      SudokuDef(field.toArray)
    }

  /**
    * Checks whether a ''SudokuDef'' is valid. This method checks whether the
    * given definition does not violate any of the Sudoku rules. If this is the
    * case, it returns a corresponding error object. Otherwise, result is a
    * ''LookupTables'' object that can be used to solve the Sudoku.
    *
    * @param sudokuDef the definition to check
    * @return the results of the check
    */
  def checkSudoku(sudokuDef: SudokuDef): Either[CheckError, LookupTables] = {
    val tables = MutableLookupTables(sudokuDef.squareSize)

    @tailrec def doCheck(row: Int, col: Int): Either[CheckError, LookupTables] = {
      if (row >= sudokuDef.fieldSize) Right(tables.toImmutable)
      else {
        val value = sudokuDef(row, col)
        if (value > sudokuDef.fieldSize) Left(ErrorInvalidNumber(row, col, value))
        else checkAllowed(value, row, col, tables) match {
          case Some(error) =>
            Left(error)

          case None =>
            if (value > 0) {
              tables.markUsed(value, row, col, used = true)
            }
            val nextCol = if (col >= sudokuDef.fieldSize - 1) 0 else col + 1
            val nextRow = if (nextCol == 0) row + 1 else row
            doCheck(nextRow, nextCol)
        }
      }
    }

    doCheck(0, 0)
  }

  /**
    * Solves the specified sudoku and returns a new definition with all
    * numbers filled in if possible. Otherwise, the passed in definition is
    * returned.
    *
    * @param sudokuDef the definition to be solved
    * @param tables    the lookup tables
    * @return the solved definition
    */
  def solve(sudokuDef: SudokuDef, tables: LookupTables): SudokuDef = {
    val field = sudokuDef.toArray
    val tab = tables.toMutable

    def solveColumn(number: Int, col: Int): Boolean = {
      if (number > sudokuDef.fieldSize) { // successful end of recursion?
        true
      } else if (col >= sudokuDef.fieldSize) { // All columns for this number have been processed
        // => next number
        solveColumn(number + 1, 0)
      } else if (tab.isColumnUsed(number, col)) { // This number is fixed placed in this column.
        solveColumn(number, col + 1)
      } else {
        // Try all combinations for this number in this column
        var row = 0
        while ( {
          row < sudokuDef.fieldSize
        }) {
          if (field(row)(col) == 0 && checkAllowed(number, row, col, tab).isEmpty) {
            field(row)(col) = number
            tab.markUsed(number, row, col, used = true)
            if (solveColumn(number, (col + 1).toShort)) return true
            field(row)(col) = 0
            tab.markUsed(number, row, col, used = false)
          }
          row += 1
        }
        false
      }
    }

    if (solveColumn(1, 0)) SudokuDef(field)
    else sudokuDef
  }

  /**
    * Checks whether a number can be placed at a position according to the
    * Sudoku rules.
    *
    * @param value  the number to be placed
    * @param row    the row index
    * @param col    the column index
    * @param tables an object with lookup tables
    * @return an option with a failure code; ''None'' if okay
    */
  private def checkAllowed(value: Int, row: Int, col: Int, tables: MutableLookupTables):
  Option[CheckError] = {
    if (value > 0) {
      if (tables.isRowUsed(value, row)) Some(ErrorRowDuplicate(row, col, value))
      else if (tables.isColumnUsed(value, col)) Some(ErrorColumnDuplicate(row, col, value))
      else if (tables.isSquareUsedAt(value, row, col))
        Some(ErrorSquareDuplicate(row, col, value))
      else None
    } else None
  }
}
