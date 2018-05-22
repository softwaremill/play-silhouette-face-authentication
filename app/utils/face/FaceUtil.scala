package utils.face

import java.io.{ BufferedOutputStream, FileOutputStream }
import java.nio.file.Files

import com.mohiva.play.silhouette.api.util.Credentials
import models.User
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData

import scala.concurrent.Future
import scala.util.Try

object FaceUtil {

  def imageAsBinaryArray(requestedData: Option[MultipartFormData[TemporaryFile]]): Array[Byte] = {
    requestedData.map { fileData =>
      fileData.file("uploadedImage").map { image =>
        val filename = image.filename
        val bytes = Files.readAllBytes(image.ref.path)
        bytes
      }.getOrElse {
        Array.emptyByteArray
      }
    }.get
  }

  def checkFace(tempFolderPath: String, signInData: Option[MultipartFormData[TemporaryFile]], credentials: Credentials, user: User): Future[Boolean] = {
    signInData.map { fileData =>
      fileData.file("uploadedImage").map { image =>
        import java.io.File
        val filename = image.filename
        image.ref.moveTo(new File(s"$tempFolderPath/signIn-$filename.png"))
        val bos = new BufferedOutputStream(new FileOutputStream(new File(s"$tempFolderPath/orig-$filename.png")))
        bos.write(user.face)
        bos.close()
        checkIfFacesMatch(s"$tempFolderPath/signIn-$filename.png", s"$tempFolderPath/orig-$filename.png")
      }.getOrElse {
        Future.successful(false)
      }
    }.get
  }

  import sys.process._
  import scala.concurrent.ExecutionContext.Implicits.global

  def checkIfFacesMatch(facePath1: String, facePath2: String): Future[Boolean] = {
    Future {
      val result = s"python python/facematch.py --face1=$facePath1 --face2=$facePath2".!!
      val res = result.split("\n").tail.headOption.exists(r => Try(r.toBoolean).getOrElse(false))
      removeFile(facePath1)
      removeFile(facePath2)
      res
    }
  }

  def removeFile(filePath: String) = {
    s"rm $filePath".!
  }
}
