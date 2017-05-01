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

import akka.actor.Actor
import de.javamagazin.akka.hello.HelloActor.GreetRequest

object HelloActor {

  /**
    * A message telling [[HelloActor]] to print a greeting for the specified
    * name.
    *
    * @param name the name for the greeting
    */
  case class GreetRequest(name: String)

}

/**
  * Simple hello world actor.
  *
  * @param greeting the string to use to greet a person
  */
class HelloActor(greeting: String) extends Actor {
  override def receive: Receive = {
    case GreetRequest(name) =>
      println(greeting + name)
  }
}
