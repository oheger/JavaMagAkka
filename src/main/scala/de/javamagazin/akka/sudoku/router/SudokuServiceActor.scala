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

import java.io.IOException

import akka.actor.SupervisorStrategy.{Resume, Stop}
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.routing.BalancingPool
import akka.pattern.ask
import akka.util.Timeout
import de.javamagazin.akka.sudoku.msg._
import de.javamagazin.akka.sudoku.router.SaveStatsActor.{SaveStatsRequest, SaveStatsResponse}
import de.javamagazin.akka.sudoku.router.SudokuServiceActor.{FailedSolveOperation, SuccessfulSolveOperation}
import de.javamagazin.akka.sudoku.solver.SudokuDef
import de.javamagazin.akka.sudoku.solver.Sudokus.SudokuException

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object SudokuServiceActor {

  /**
    * Internally used message class indicating a failed solve operation.
    *
    * @param client    the client actor
    * @param sudokuDef the failed definition
    * @param exception the exception
    */
  private case class FailedSolveOperation(client: ActorRef, sudokuDef: SudokuDef,
                                          exception: Throwable)

  /**
    * Internally used message class indicating a successful solve operation.
    *
    * @param client    the client actor
    * @param sudokuDef the solved sudoku definition
    * @param time      the time for the solve operation
    */
  private case class SuccessfulSolveOperation(client: ActorRef, sudokuDef: SudokuDef,
                                              time: Long)

}

/**
  * An actor providing a service to solve sudokus.
  *
  * The actor manages child actors (actually routers) to check and solve
  * sudokus. That way multiple requests are automatically processed in
  * parallel. It also keeps track of some statistics.
  */
class SudokuServiceActor extends Actor {
  /** The child actor for checking sudoku definitions. */
  private var checkActor: ActorRef = _

  /** The child actor for solving sudoku definitions. */
  private var solveActor: ActorRef = _

  /**
    * A map which stores the currently active write operations for stats
    * data. Keys are writer actors, values are the requesting clients.
    */
  private var activeWriteOps = Map.empty[ActorRef, ActorRef]

  /** The number of solved sudokus. */
  private var solvedCount = 0

  /** The number of failed operations. */
  private var failedCount = 0

  /** The accumulated time for successful operations. */
  private var accTime = 0L

  /** Timeout for ask operations. */
  private implicit val timeout = Timeout(10.seconds)

  /** A custom supervisor strategy. */
  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case _: IOException => Stop
    case _ => Resume
  }

  override def preStart(): Unit = {
    super.preStart()
    checkActor = context.actorOf(BalancingPool(4).props(Props[SudokuCheckActor]),
      "sudokuCheckActor")
    solveActor = context.actorOf(BalancingPool(8).props(Props[SudokuSolveActor]),
      "sudokuSolveActor")
  }

  override def receive: Receive = {
    case SolveSudoku(sudokuDef) =>
      processSudoku(sudokuDef)

    case SuccessfulSolveOperation(client, sudokuDef, time) =>
      client ! SolveSudokuResponse(sudokuDef, None, time)
      accTime += time
      solvedCount += 1

    case FailedSolveOperation(client, sudokuDef, exception) =>
      client ! SolveSudokuResponse(sudokuDef, Some(exception), 0)
      failedCount += 1

    case WriteStats(path) =>
      val writerActor = context.actorOf(Props[SaveStatsActor])
      context watch writerActor
      writerActor ! SaveStatsRequest(path, solvedCount, failedCount, accTime,
        sender())
      activeWriteOps += writerActor -> sender()

    case SaveStatsResponse(request) =>
      request.client ! StatsWritten
      context unwatch sender()
      context stop sender()
      activeWriteOps -= sender()

    case Terminated(actor) =>
      activeWriteOps.get(actor) foreach (_ ! WriteStatsFailed)
      activeWriteOps -= actor
  }

  /**
    * Processes a request to solve a sudoku by delegating to the child
    * actors.
    *
    * @param sudokuDef the definition to be processed
    */
  private def processSudoku(sudokuDef: SudokuDef): Unit = {
    import context.dispatcher
    val client = sender()
    val futCheck = (checkActor ? CheckSudoku(sudokuDef)).mapTo[CheckSudokuResponse]
    futCheck.flatMap(createSolveRequest) onComplete {
      case Success(resp) =>
        self ! SuccessfulSolveOperation(client, resp.sudokuDef, resp.time)
      case Failure(exception) =>
        self ! FailedSolveOperation(client, sudokuDef, exception)
    }
  }

  /**
    * Sends a request to solve a sudoku based on a successful check response.
    * If the check was not successful, an exception is thrown causing the
    * ''Future'' to fail.
    *
    * @param checkResp the message with the check response
    * @return a ''Future'' for the solve request
    */
  private def createSolveRequest(checkResp: CheckSudokuResponse): Future[SolveSudokuResponse] =
    checkResp.checkResult match {
      case Right(tables) =>
        (solveActor ? SolveCheckedSudoku(checkResp.sudokuDef, tables)).mapTo[SolveSudokuResponse]
      case Left(error) =>
        throw new SudokuException(error)
    }
}
