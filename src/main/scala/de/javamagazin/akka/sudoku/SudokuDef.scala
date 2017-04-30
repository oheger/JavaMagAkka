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

package de.javamagazin.akka.sudoku

object SudokuDef {
  /**
    * Creates a new instance of ''SudokuDef'' from the specified array with
    * cell values. It is checked whether the array has valid dimensions.
    * If not, an exception is thrown.
    *
    * @param field the array representing the cells of the Sudoku
    * @return the new ''SudokuDef'' instance
    * @throws IllegalArgumentException if the dimensions of the array are
    *                                  invalid
    */
  def apply(field: Array[Array[Int]]): SudokuDef = {
    val sqrt = math.sqrt(field.length).toInt
    if (sqrt * sqrt != field.length) {
      throw new IllegalArgumentException("Field dimension is not a square number!")
    }
    if (field.exists(_.length != field.length)) {
      throw new IllegalArgumentException("Field contains a column with invalid number of cells!")
    }

    new SudokuDef(cloneArray(field), sqrt)
  }
}

/**
  * A class representing a Sudoku definition.
  *
  * The class stores the values of the single fields the Sudoku consists of.
  * Read-only access to single cells is possible.
  *
  * @param field      the underlying array with the cells of the Sudoku field
  * @param squareSize the size of the squares
  */
class SudokuDef private(private val field: Array[Array[Int]], val squareSize: Int) {
  /**
    * Returns the size of the whole Sudoku field.
    *
    * @return the size of the field
    */
  def fieldSize: Int = field.length

  /**
    * Allows access to the value of a specific cell. The specified indices must
    * be in the range [0, ''fieldSize''); otherwise, an exception is thrown.
    * For an undefined cell the value 0 is returned.
    *
    * @param row the row index (0-based)
    * @param col the column index (0-based)
    * @return the value of this cell
    * @throws ArrayIndexOutOfBoundsException if indices are invalid
    */
  def apply(row: Int, col: Int): Int = field(row)(col)

  /**
    * Returns an array with the content of this ''SudokuDef''. This is a
    * copy of the internal state of this object.
    *
    * @return an array with the content of this ''SudokuDef''
    */
  def toArray: Array[Array[Int]] = cloneArray(field)

  /**
    * Returns a flag whether this definition is already solved. This is the
    * case if all cells are filled with numbers.
    *
    * @return a flag whether this definition is solved
    */
  def solved: Boolean =
    field forall { arr => arr forall (_ > 0) }

  def canEqual(other: Any): Boolean = other.isInstanceOf[SudokuDef]

  override def equals(other: Any): Boolean = other match {
    case that: SudokuDef =>
      (that canEqual this) &&
        squareSize == that.squareSize &&
        equalsFields(field, that.field)
    case _ => false
  }

  override def hashCode(): Int = {
    val state = field.map(java.util.Arrays.hashCode)
    state.foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString: String =
    field.map(_.mkString(", ")).mkString("\n")

  /**
    * Checks whether the given arrays are equal.
    *
    * @param f1 array 1
    * @param f2 array 2
    * @return a flag whether these arrays are equal
    */
  private def equalsFields(f1: Array[Array[Int]], f2: Array[Array[Int]]): Boolean =
    f1.zip(f2).forall(t => java.util.Arrays.equals(t._1, t._2))
}
