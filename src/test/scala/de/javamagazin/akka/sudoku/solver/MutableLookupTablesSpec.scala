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

import org.scalatest.{FlatSpec, Matchers}

/**
  * Test class for ''MutableLookupTables''.
  */
class MutableLookupTablesSpec extends FlatSpec with Matchers {
  "A MutableLookupTables" should "produce an empty instance" in {
    val tab = MutableLookupTables(3)
    for {v <- 1 to 9
         i <- 0 until 9
    } {
      tab.isRowUsed(v, i) shouldBe false
      tab.isColumnUsed(v, i) shouldBe false
      tab.isSquareUsed(v, i) shouldBe false
    }
  }

  it should "produce an instance from existing arrays" in {
    val rows = Array.ofDim[Boolean](4, 4)
    val cols = Array.ofDim[Boolean](4, 4)
    val squares = Array.ofDim[Boolean](4, 4)
    rows(1)(1) = true
    cols(2)(2) = true
    squares(3)(3) = true
    val tab = MutableLookupTables(2, rows, cols, squares)
    rows(1)(1) = false
    cols(2)(2) = false
    squares(3)(3) = false

    tab.isRowUsed(1, 0) shouldBe false
    tab.isRowUsed(2, 1) shouldBe true
    tab.isColumnUsed(1, 0) shouldBe false
    tab.isColumnUsed(3, 2) shouldBe true
    tab.isSquareUsed(1, 0) shouldBe false
    tab.isSquareUsed(4, 3) shouldBe true
  }

  it should "allow chaning the used state of numbers and cells" in {
    val tab = MutableLookupTables(3)

    tab.markUsed(3, 1, 4, used = true)
    tab.markUsed(9, 8, 8, used = true)
    tab.markUsed(8, 4, 4, used = true)
    tab.markUsed(8, 4, 4, used = false)

    tab.isRowUsed(3, 1) shouldBe true
    tab.isColumnUsed(3, 4) shouldBe true
    tab.isSquareUsed(9, 8) shouldBe true
    tab.isRowUsed(8, 4) shouldBe false
  }

  it should "support transformation to an immutable table" in {
    val tab = MutableLookupTables(3)
    tab.markUsed(3, 1, 4, used = true)
    tab.markUsed(9, 8, 8, used = true)
    tab.markUsed(8, 4, 4, used = true)

    val tab2 = tab.toImmutable.toMutable
    tab2.squareSize should be(tab.squareSize)
    for {v <- 1 to 9
         i <- 0 until 9
    } {
      tab2.isRowUsed(v, i) shouldBe tab.isRowUsed(v, i)
      tab2.isColumnUsed(v, i) shouldBe tab.isColumnUsed(v, i)
      tab2.isSquareUsed(v, i) shouldBe tab.isSquareUsed(v, i)
    }
  }

  it should "copy arrays when transforming to an immutable table" in {
    val tab = MutableLookupTables(3)
    tab.markUsed(3, 1, 1, used = true)
    val immutableTab = tab.toImmutable

    tab.markUsed(3, 1, 1, used = false)
    val tab2 = immutableTab.toMutable
    tab2.isRowUsed(3, 1) shouldBe true
    tab2.isColumnUsed(3, 1) shouldBe true
    tab2.isSquareUsed(3, 0) shouldBe true
  }
}
