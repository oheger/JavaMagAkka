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

package de.javamagazin.akka

import scala.reflect.ClassTag

/**
  * A package object with some utility methods used by multiple classes in
  * this package.
  */
package object sudoku {
  /**
    * Generic clone method for 2d arrays. This is needed by multiple classes
    * that have to do defensive copies of arrays.
    *
    * @param arr the array to be cloned
    * @param c   the class tag
    * @tparam T the element type of the array
    * @return the cloned array
    */
  def cloneArray[T](arr: Array[Array[T]])(implicit c: ClassTag[T]): Array[Array[T]] =
    arr.map { a =>
      val copy = new Array[T](a.length)
      System.arraycopy(a, 0, copy, 0, a.length)
      copy
    }
}
