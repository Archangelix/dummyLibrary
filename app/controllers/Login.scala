package controllers

import models.User
import models.postgre.DBUser
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.mvc.Action
import play.api.mvc.Controller

object Login extends Controller {

  case class FormUser(username: String, password: String)
  
  val loginForm = Form[FormUser](
      mapping (
          "username" -> nonEmptyText,
          "password" -> nonEmptyText
      )(FormUser.apply)(FormUser.unapply)
      verifying("Incorrect user / password combination.", {
    	  user => {
    	    try {
    	    	val dbUser = DBUser.findByUserID(user.username)
    	    	println ("Database password = "+dbUser.password)
	    		dbUser.password.equals(user.password)
    	    } catch {
    	      case e: Exception => {
    	        println ("User cannot be found.")
    	        false
    	      }
    	    }
    	  }
      	}
      )
  )
  
  def loginPage = Action {
    Ok(views.html.login(loginForm))
  }
  
  def login = Action { implicit req =>
    val tempForm = loginForm.bindFromRequest
    tempForm.fold (
        errors => {
          BadRequest (views.html.login(tempForm))
        },
        data => {
          Redirect(routes.Application.index)
        }
    )
  }
  
}