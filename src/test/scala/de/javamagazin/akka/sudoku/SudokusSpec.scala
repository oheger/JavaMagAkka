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

import de.javamagazin.akka.sudoku.Sudokus.{ErrorColumnDuplicate, ErrorInvalidNumber,
ErrorRowDuplicate, ErrorSquareDuplicate}
import org.scalatest.{FlatSpec, Matchers}

object SudokusSpec {
  /** A test Sudoku request definition. */
  private val TestRequest = Sudokus.sudoku(
    "0,1,0,7,5,8,6,0,0",
    "2, 0, 7, 0, 6, 0, 4, 0, 0",
    "0,3,0,0,9,4,1,0,0",
    "5,0,1,0,0,0,0,2,0",
    "9,0,0,8,0,7,0,0,4",
    "0,2,0,0,0,0,9,0,3",
    "0,0,2,6,4,0,0,5,0",
    "0,0,6,0,7,0,8,0,1",
    "0,0,4,3,8,9,0,7,0").get

  /** The solution for the test request. */
  private val Solution = Sudokus.sudoku(
    "4,1,9,7,5,8,6,3,2",
    "2,8,7,1,6,3,4,9,5",
    "6,3,5,2,9,4,1,8,7",
    "5,4,1,9,3,6,7,2,8",
    "9,6,3,8,2,7,5,1,4",
    "7,2,8,4,1,5,9,6,3",
    "8,7,2,6,4,1,3,5,9",
    "3,9,6,5,7,2,8,4,1",
    "1,5,4,3,8,9,2,7,6"
  ).get

  /**
    * Returns the lookup tables for the test Sudoku definition.
    *
    * @return the lookup tables
    */
  private def testLookupTables(): LookupTables = {
    Sudokus.checkSudoku(TestRequest) match {
      case Right(tables) => tables
      case e => throw new AssertionError("Unexpected result: " + e)
    }
  }
}

/**
  * Test class for ''Sudokus''.
  */
class SudokusSpec extends FlatSpec with Matchers {

  import SudokusSpec._

  "Sudokus" should "detect an invalid number" in {
    val arr = Array.ofDim[Int](9, 9)
    arr(1)(7) = 10
    val sudokuDef = SudokuDef(arr)

    Sudokus.checkSudoku(sudokuDef) should be(Left(ErrorInvalidNumber(1, 7, 10)))
  }

  it should "detect a duplicate number in a row" in {
    val arr = Array.ofDim[Int](9, 9)
    arr(0) = Array(5, 0, 3, 0, 0, 4, 0, 5, 0)
    val sudokuDef = SudokuDef(arr)

    Sudokus.checkSudoku(sudokuDef) should be(Left(ErrorRowDuplicate(0, 7, 5)))
  }

  it should "detect a duplicate number in a column" in {
    val arr = Array.ofDim[Int](9, 9)
    arr(1)(1) = 2
    arr(3)(1) = 7
    arr(5)(1) = 4
    arr(6)(1) = 3
    arr(8)(1) = 2
    val sudokuDef = SudokuDef(arr)

    Sudokus.checkSudoku(sudokuDef) should be(Left(ErrorColumnDuplicate(8, 1, 2)))
  }

  it should "detect a duplicate number in a square" in {
    val arr = Array.ofDim[Int](9, 9)
    arr(6)(3) = 2
    arr(6)(4) = 7
    arr(6)(5) = 4
    arr(7)(4) = 3
    arr(8)(3) = 8
    arr(8)(4) = 2
    val sudokuDef = SudokuDef(arr)

    Sudokus.checkSudoku(sudokuDef) should be(Left(ErrorSquareDuplicate(8, 4, 2)))
  }

  it should "produce a correct lookup table during verification" in {
    val arr = Array.ofDim[Int](9, 9)
    arr(0) = Array(5, 0, 3, 0, 0, 4, 0, 8, 0)
    val sudokuDef = SudokuDef(arr)

    Sudokus.checkSudoku(sudokuDef) match {
      case Right(tables) =>
        tables.squareSize should be(3)
        val mt = tables.toMutable
        mt.isRowUsed(5, 0) shouldBe true
        mt.isColumnUsed(3, 2) shouldBe true
        mt.isSquareUsed(4, 1) shouldBe true
        mt.isRowUsed(7, 0) shouldBe false
      case e =>
        fail("Unexpected result: " + e)
    }
  }

  it should "solve a sudoku" in {
    Sudokus.solve(TestRequest, testLookupTables()) should be(Solution)
  }

  it should "return the same definition if it cannot be solved" in {
    val mt = testLookupTables().toMutable
    (1 to 9).foreach(i => mt.markUsed(1, i - 1, 3, used = true))

    Sudokus.solve(TestRequest, mt.toImmutable) should be theSameInstanceAs TestRequest
  }
}
