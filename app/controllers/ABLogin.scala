package controllers

import models.OBUser
import models.OBUserRole
import models.exception.UserNotFoundException
import models.form.FormUserPassword
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Flash
import play.api.mvc.Security
import play.api.mvc.WithHeaders
import play.mvc.Http.Session
import services.DBService
import java.security.MessageDigest
import common.SecurityUtil
import play.api.data.FormError

/**
 * Action to handle the logging section.
 */
object ABLogin extends Controller {

	def sha256(pKey: String) = {
	  val digest = MessageDigest.getInstance("SHA-256")
	  val hash = digest.digest(pKey.getBytes("UTF-8"))
	  hash.toString
	}
	
  val loginForm = Form[FormUserPassword](
      mapping (
          "username" -> nonEmptyText,
          "password" -> nonEmptyText
      )(FormUserPassword.apply)(FormUserPassword.unapply)
  )
  
  /**
   * Displaying the login page.
   */
  def loginPage = Action { implicit req =>
    Ok(views.html.login(loginForm))
  }
  
  /**
   * Login authentication.
   */
  def login = Action { implicit req =>
    val tempForm = loginForm.bindFromRequest
    tempForm.fold (
      error => {
        BadRequest(views.html.login(error))
      },
      data => {
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
        	try {
        		val formUsername = data.username
				val dbUser = DBService.findByUserID(formUsername)
				val role = dbUser.role
				if (role.equals(OBUserRole.ADMIN)) {
					Redirect(routes.ABCatalogList.index).withSession(Security.username -> formUsername)
				} else {
					Redirect(routes.ABSearchCatalog.index).withSession(Security.username -> formUsername)
				}
        	} catch {
        		case e: Exception => {
        			e.printStackTrace()
        			BadRequest (views.html.login(tempForm))
        		}
        	}
        } else {
          val errorForm = Form(tempForm.mapping, 
              Map("username"->data.username, "password" -> ""), 
              Seq(new FormError("", "Invalid user / password" )), tempForm.value)
          BadRequest (views.html.login(errorForm))
        }
      }
    )
  }
  
  def logout = Action { implicit req =>
    Redirect(routes.ABLogin.loginPage).withNewSession.flashing("message" -> "Log out successful!")
  }
  
}