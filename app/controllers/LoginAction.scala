package controllers

import models.User
import models.db.DBUser
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.mvc.Action
import play.api.mvc.Controller
import models.db.DBUserPassword
import services.DBService
import play.mvc.Http.Session

/**
 * Action to handle the logging section.
 */
object LoginAction extends Controller {

  val loginForm = Form[FormUser](
      mapping (
          "username" -> nonEmptyText,
          "password" -> nonEmptyText
      )(FormUser.apply)(FormUser.unapply)
      verifying ("Invalid user / password", { user =>
        val dbPassword = DBService.getPassword(user.username)
        dbPassword.equals(user.password)
      })
  )
  
  /**
   * Displaying the login page.
   */
  def loginPage = Action {
    Ok(views.html.login(loginForm))
  }
  
  /**
   * Login authentication.
   */
  def login = Action { implicit req =>
    val tempForm = loginForm.bindFromRequest
    tempForm.fold (
      error => {
        BadRequest (views.html.login(error))
      },
      data => {
	    try {
	      val formUsername = data.username
	      val dbUser = DBService.findByUserID(formUsername)
    	  Redirect(routes.CatalogListAction.index).withSession(
    	      session + ("username" -> formUsername)
    	  )
	    } catch {
	      case e: Exception => {
	        e.printStackTrace()
	        BadRequest (views.html.login(tempForm))
	      }
	    }
      }
    )
  }
  
}