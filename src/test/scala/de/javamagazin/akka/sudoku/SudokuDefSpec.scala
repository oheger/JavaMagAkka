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

import org.scalatest.{FlatSpec, Matchers}

/**
  * Test class for ''SudokuDef''
  */
class SudokuDefSpec extends FlatSpec with Matchers {
  "A SudokuDef" should "throw for an array with invalid column dimensions" in {
    intercept[IllegalArgumentException] {
      val field = Array(new Array[Int](9), new Array[Int](9), new Array[Int](9),
        new Array[Int](9), new Array[Int](9), new Array[Int](9),
        new Array[Int](9), new Array[Int](8), new Array[Int](9))
      SudokuDef(field)
    }
  }

  it should "throw for row sizes which are no square numbers" in {
    val field = Array(new Array[Int](8), new Array[Int](8), new Array[Int](8),
      new Array[Int](8), new Array[Int](8), new Array[Int](8),
      new Array[Int](8), new Array[Int](8))
    intercept[IllegalArgumentException] {
      SudokuDef(field)
    }
  }

  it should "return the correct field size" in {
    val field = Array.ofDim[Int](9, 9)
    val sudoku = SudokuDef(field)

    sudoku.fieldSize should be(9)
  }

  it should "calculate the correct square size" in {
    val field = Array.ofDim[Int](9, 9)
    val sudoku = SudokuDef(field)

    sudoku.squareSize should be(3)
  }

  it should "allow access to single cells" in {
    val field = Array.ofDim[Int](4, 4)
    field(0)(1) = 1
    field(1)(2) = 2
    val sudoku = SudokuDef(field)

    sudoku(0, 0) should be(0)
    sudoku(0, 1) should be(1)
    sudoku(1, 2) should be(2)
  }

  it should "create a defensive copy of the passed in array" in {
    val field = Array.ofDim[Int](4, 4)
    field(0)(1) = 1
    field(1)(2) = 2
    val sudoku = SudokuDef(field)

    field(0)(0) = 3
    field(1)(2) = 1
    sudoku(0, 0) should be(0)
    sudoku(1, 2) should be(2)
  }

  it should "indicate that it is not solved if there are undefined cells" in {
    val field = Array(Array(1, 2, 3, 4), Array(2, 3, 4, 1),
    Array(3, 4, 1, 2), Array(4, 1, 2, 0))
    val sudoku = SudokuDef(field)

    sudoku.solved shouldBe false
  }

  it should "detect a solved definition" in {
    val field = Array(Array(1, 2, 3, 4), Array(2, 3, 4, 1),
      Array(3, 4, 1, 2), Array(4, 1, 2, 3))
    val sudoku = SudokuDef(field)

    sudoku.solved shouldBe true
  }

  it should "detect definitions that are not equal" in {
    SudokuDef(Array.ofDim[Int](9, 9)) should not be SudokuDef(Array.ofDim[Int](4, 4))

    val field1 = Array.ofDim[Int](4, 4)
    field1(0)(1) = 1
    field1(1)(2) = 2
    val sudoku1 = SudokuDef(field1)
    val field2 = Array.ofDim[Int](4, 4)
    field2(0)(1) = 1
    field2(1)(2) = 3
    val sudoku2 = SudokuDef(field2)
    sudoku1 should not be sudoku2
  }

  it should "detect equal definitions" in {
    val field1 = Array.ofDim[Int](4, 4)
    field1(0)(1) = 1
    field1(1)(2) = 2
    val sudoku1 = SudokuDef(field1)
    val field2 = Array.ofDim[Int](4, 4)
    field2(0)(1) = 1
    field2(1)(2) = 2
    val sudoku2 = SudokuDef(field1)

    sudoku1 should be(sudoku2)
    sudoku1.hashCode() should be(sudoku2.hashCode())
  }
}
