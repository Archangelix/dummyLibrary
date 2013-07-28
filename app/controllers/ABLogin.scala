package controllers

import models.OBUser
import models.db.DBUser
import models.form.FormUserPassword
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.mvc.Action
import play.api.mvc.Controller
import models.db.DBUserPassword
import services.DBService
import play.mvc.Http.Session
import play.api.mvc.Security

/**
 * Action to handle the logging section.
 */
object ABLogin extends Controller {

  val loginForm = Form[FormUserPassword](
      mapping (
          "username" -> nonEmptyText,
          "password" -> nonEmptyText
      )(FormUserPassword.apply)(FormUserPassword.unapply)
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
	      session.get("abc")
	      val formUsername = data.username
	      val dbUser = DBService.findByUserID(formUsername)
    	  Redirect(routes.ABCatalogList.index).withSession(Security.username -> formUsername)
	    } catch {
	      case e: Exception => {
	        e.printStackTrace()
	        BadRequest (views.html.login(tempForm))
	      }
	    }
      }
    )
  }
  
  def logout = Action { implicit req =>
    Redirect(routes.ABLogin.loginPage).withNewSession.flashing("message" -> "Log out successful.")
  }
  
}