package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import org.webjars.play.WebJarsUtil
import play.api.i18n.I18nSupport
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.Future

/**
 * The basic application controller.
 *
 * @param components  The Play controller components.
 * @param silhouette  The Silhouette stack.
 * @param webJarsUtil The webjar util.
 * @param assets      The Play assets finder.
 */
class ApplicationController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv]
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder
) extends AbstractController(components) with I18nSupport {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

  def uploadFile = Action { implicit request =>
    val temporaryFile: Option[MultipartFormData[TemporaryFile]] = request.body.asMultipartFormData
    println("Temporary file: " + temporaryFile)
    val result = uploadData(temporaryFile)
    Ok(result)
  }

  def uploadData(requestedData: Option[MultipartFormData[TemporaryFile]]): String = {
    requestedData.map { fileData =>
      fileData.file("uploadedImage").map { image =>
        import java.io.File
        println("IMG: " + image)
        val filename = image.filename
        val contentType = image.contentType
        println("RECEiVED File: " + filename)
        image.ref.moveTo(new File(s"/home/kris/Pictures/upload.png"))
        "File uploaded"
      }.getOrElse {
        "Missing file"
      }
    }.get
  }
}
