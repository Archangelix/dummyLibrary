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

object Login extends Controller {

  case class FormUser(username: String, password: String)
  
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
  
  def loginPage = Action {
    Ok(views.html.login(loginForm))
  }
  
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
    	  Redirect(routes.Application.index).withSession(
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