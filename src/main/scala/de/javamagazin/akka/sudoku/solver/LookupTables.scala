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

/**
  * A class storing information about occupied cells.
  *
  * This information is used during Sudoku solving. It allows a fast check
  * whether a number can be placed at a specific position.
  *
  * The information consists of several 2-dimensional boolean arrays. The
  * first index in such an array selects a number. The second index references
  * a specific position (row, column, or square). A value of '''true''' means
  * that this number is already present at this position.
  *
  * Instances of this class are created from [[MutableLookupTables]] objects.
  */
class LookupTables private[sudoku](val squareSize: Int,
                                   filledRows: Array[Array[Boolean]],
                                   filledColumns: Array[Array[Boolean]],
                                   filledSquares: Array[Array[Boolean]]) {
  /**
    * Returns a mutable lookup table initialized from the data stored in
    * this instance.
    *
    * @return the ''MutableLookupTables'' based on this object
    */
  def toMutable: MutableLookupTables =
    MutableLookupTables(squareSize, filledRows, filledColumns, filledSquares)
}

object MutableLookupTables {
  /**
    * Creates a new instance of ''MutableLookupTables'' with unused cells
    * of the specified square size.
    *
    * @param squareSize the square size
    * @return the new instance
    */
  def apply(squareSize: Int): MutableLookupTables = {
    val fieldSize = squareSize * squareSize
    new MutableLookupTables(squareSize, Array.ofDim[Boolean](fieldSize, fieldSize),
      Array.ofDim[Boolean](fieldSize, fieldSize),
      Array.ofDim[Boolean](fieldSize, fieldSize))
  }

  /**
    * Creates a new instance of ''MutableLookupTables'' with the specified
    * data.
    *
    * @param squareSize    the square size
    * @param filledRows    the array for filled rows
    * @param filledColumns the array for filled columns
    * @param filledSquares the array for filled squares
    * @return the new instance
    */
  def apply(squareSize: Int, filledRows: Array[Array[Boolean]],
            filledColumns: Array[Array[Boolean]],
            filledSquares: Array[Array[Boolean]]): MutableLookupTables =
    new MutableLookupTables(squareSize, cloneArray(filledRows), cloneArray(filledColumns),
      cloneArray(filledSquares))
}

/**
  * A class storing information about occupied cells and allowing to manipulate
  * this data.
  *
  * This class is used internally during Sudoku verification and solving.
  *
  * @param squareSize    the square size
  * @param filledRows    the array for filled rows
  * @param filledColumns the array for filled columns
  * @param filledSquares the array for filled squares
  */
class MutableLookupTables private(val squareSize: Int,
                                  filledRows: Array[Array[Boolean]],
                                  filledColumns: Array[Array[Boolean]],
                                  filledSquares: Array[Array[Boolean]]) {
  def isRowUsed(value: Int, row: Int): Boolean = filledRows(value - 1)(row)

  def isColumnUsed(value: Int, col: Int): Boolean = filledColumns(value - 1)(col)

  def isSquareUsed(value: Int, idx: Int): Boolean = filledSquares(value - 1)(idx)

  def isSquareUsedAt(value: Int, row: Int, col: Int): Boolean =
    isSquareUsed(value, getSquareIndex(row, col))

  /**
    * Sets the used flag for a value at the specified position. This means
    * that this number is placed at this position which restricts other
    * placements according to Sudoku rules.
    *
    * @param value the value
    * @param row   the row index
    * @param col   the column index
    * @param used  flag whether the number should be placed or removed
    */
  def markUsed(value: Int, row: Int, col: Int, used: Boolean): Unit = {
    val valIdx = value - 1
    filledRows(valIdx)(row) = used
    filledColumns(valIdx)(col) = used
    filledSquares(valIdx)(getSquareIndex(row, col)) = used
  }

  /**
    * Returns a ''LookupTables'' object with the data of this instance.
    *
    * @return the ''LookupTables'' object
    */
  def toImmutable: LookupTables =
    new LookupTables(squareSize, cloneArray(filledRows), cloneArray(filledColumns),
      cloneArray(filledSquares))

  /**
    * Determines the index for a square based on the specified position.
    *
    * @param row the row index
    * @param col the column index
    * @return the index of the corresponding square
    */
  private def getSquareIndex(row: Int, col: Int): Int =
    (row / squareSize) * squareSize + col / squareSize
}
