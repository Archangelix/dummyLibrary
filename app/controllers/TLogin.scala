package controllers

import models.OBUser
import models.OBUserRole
import models.exception.UserNotFoundException
import models.form.FormUserPassword
import play.api.data.Form
import play.api.data.format.Formats._
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Flash
import play.api.mvc.Results._
import play.api.mvc.Security
import play.api.mvc.WithHeaders
import play.mvc.Http.Session
import services.DBService
import java.security.MessageDigest
import common.SecurityUtil._
import common.CommonUtil._
import controllers.ABUserDetail.MODE_ADD
import controllers.ABUserDetail.MODE_EDIT
import models.form.FormUser
import java.util.Date
import play.api.data.FormError
import play.api.data.validation.Constraint
import play.api.data.validation.Invalid
import play.api.data.validation.Valid
import play.api.data.validation.ValidationError
import org.joda.time.DateTime
import org.joda.time.Period
import play.api.Routes
import common.SecurityUtil

trait TLogin extends Controller {

  val formUserLoginMapping = mapping (
          "username" -> nonEmptyText,
          "password" -> nonEmptyText
      )(FormUserPassword.apply)(FormUserPassword.unapply)
      
  val loginForm = Form[FormUserPassword](formUserLoginMapping)
  
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
           	val dbPassword = DBService.getPassword(data.username.toUpperCase())
        	val words = dbPassword.split('|')
        	println("DB Passwords = "+words)
        	val salt = words(0)
        	val saltedDBPassword = words(1)
        	val encryptedUserPwd = SecurityUtil.hex_digest(salt+data.password)
        	println("User salted password = "+encryptedUserPwd)
  			saltedDBPassword.equals(encryptedUserPwd)
        } catch {
        	case e: UserNotFoundException => false
        }
        
        if (validLogin) {
          // Correct password. Redirect the user to the respective home page according to the role.
          try {
            val formUsername = data.username
            val dbUser = DBService.findByUserID(formUsername)
            val role = dbUser.role
            if (role.equals(OBUserRole.ADMIN)) {
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
  
  /**
   * Log out the user. Bring the user back to the login page.
   */
  def logout = Action { implicit req =>
    Redirect(routes.ABLogin.loginPage).withNewSession.flashing("message" -> "Log out successful!")
  }
  
}