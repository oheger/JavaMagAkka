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

package de.javamagazin.akka.hello

import akka.actor.{ActorSystem, Props}
import de.javamagazin.akka.hello.HelloActor.GreetRequest

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Main program for the hello world actor.
  */
object HelloMain {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("HelloSystem")
    val actor = system.actorOf(Props(classOf[HelloActor], "Hello, "))
    actor ! GreetRequest("World")

    Thread.sleep(1000)
    val futTerm = system.terminate()
    Await.ready(futTerm, 5.seconds)
  }
}
