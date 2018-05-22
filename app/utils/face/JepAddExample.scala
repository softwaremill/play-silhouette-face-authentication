package utils.face

import jep.Jep

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.util.Failure

/*
sbt "runMain utils.face.JepAddExample"
 */
object JepAddExample extends App {

  import sys.process._
  import scala.concurrent.ExecutionContext.Implicits.global

  def checkIfFacesMatch(facePath1: String, facePath2: String): Future[String] = {
    Future {
      val result = s"python python/facematch.py --face1=$facePath1 --face2=$facePath2".!!
      println("result: ***" + result + " ***")
      result.split("\n").tail.head
    }
  }

  val path1 = "/home/kris/Pictures/orig-274c4073-55a7-41ff-958b-2f95b0cb8829.png"
  val path2 = "/home/kris/Pictures/signIn-2d25c7e2-6349-4598-855c-555ffbffbfc3.png"
  println("RES: " + Await.result(checkIfFacesMatch(path1, path2), Duration.Inf))

  //

}
