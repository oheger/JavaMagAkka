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

import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorRef}

object SaveStatsActor {

  /**
    * A message to request that a file with statistics data is written.
    *
    * @param path        the path to the file
    * @param solvedCount the number of solved sudokus
    * @param failedCount the number of failed sudokus
    * @param accTime     the accumulated time
    * @param client      the client actor
    */
  case class SaveStatsRequest(path: String, solvedCount: Int, failedCount: Int,
                              accTime: Long, client: ActorRef)

  /**
    * A response message sent by this actor after the file has been written
    * successfully.
    *
    * @param request the associated request
    */
  case class SaveStatsResponse(request: SaveStatsRequest)

  /**
    * Generates the content of the file with statistical information.
    *
    * @param stats the object with the request to write the file
    * @return the content of the file
    */
  private def generateStatsFileContent(stats: SaveStatsRequest): String = {
    val average = if (stats.solvedCount == 0) 0
    else stats.accTime / stats.solvedCount
    s"""Successful: ${stats.solvedCount}
       |Failed: ${stats.failedCount}
       |Average time: $average ns
   """.
      stripMargin
  }
}

/**
  * Actor class for saving statistics information about the Sudoku solver
  * service.
  */
class SaveStatsActor extends Actor {

  import SaveStatsActor._

  override def receive: Receive = {
    case stats: SaveStatsRequest =>
      val path = Paths.get(stats.path)
      val content = generateStatsFileContent(stats)
      Files.write(path, java.util.Collections.singletonList(content))
      sender ! SaveStatsResponse(stats)
  }
}
