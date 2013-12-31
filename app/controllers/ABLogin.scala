package controllers

import play.api.mvc.Controller
import utils.CommonUtil._
import play.api.mvc.Action
import utils.SecurityUtil._
import models.exception.UserNotFoundException
import models.common.UserRole
import play.api.mvc.Security
import play.api.data.Form
import play.api.data.FormError

trait ABLogin extends TLogin { this: Controller => 
  
  /**
   * Displaying the login page.
   */
  def loginPage = Action { implicit req =>
    Ok(views.html.index(loginForm)(session))
  }
  
  /**
   * Login authentication.
   */
  def login = Action { implicit req =>
    val tempForm = loginForm.bindFromRequest
    tempForm.fold (
      error => {
        Redirect(routes.ABLoginSignup.loginPage).flashing("username" -> loginForm.data("username"))
      },
      data => {
        // Authenticate the user password.
        val validLogin = try {
           	val dbPassword = dbService.getPassword(data.username)
        	val words = dbPassword.split('|')
        	logger.debug("DB Passwords = "+words)
        	val salt = words(0)
        	val saltedDBPassword = words(1)
        	val encryptedUserPwd = hex_digest(salt+data.password)
        	logger.debug("User salted password = "+encryptedUserPwd)
  			saltedDBPassword.equals(encryptedUserPwd)
        } catch {
        	case e: UserNotFoundException => false
        }
        
        if (validLogin) {
          // Correct password. Redirect the user to the respective home page according to the role.
          try {
            val formUsername = data.username
            val user = objUser.findByUserID(formUsername)
            val role = user.role
            if (role.equals(UserRole.ADMIN)) {
              Redirect(routes.ABUserList.listUsers).withSession(
                  Security.username -> formUsername,
                  "menuType" -> role.toString)
            } else {
              Redirect(routes.ABLogin.loginPage).withSession(
                  Security.username -> formUsername,
                  "menuType" -> role.toString)
            }
          } catch {
            case e: Exception => {
              e.printStackTrace()
              Redirect(routes.ABLoginSignup.loginPage).flashing("username" -> data.username)
            }
          }
        } else {
          // Wrong password. Return to login page.
          val errorForm = Form(tempForm.mapping, 
              Map("username"->data.username, "password" -> ""), 
              Seq(new FormError("", "Invalid user / password" )), tempForm.value)
          Redirect(routes.ABLoginSignup.loginPage).flashing("username" -> data.username)
        }
      }
    )
  }
  
}

/**
 * Action to handle the logging section.
 */
object ABLogin extends Controller with ABLogin {
	// Handled in trait TLogin
}