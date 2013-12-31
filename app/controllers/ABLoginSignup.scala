package controllers

import models.OBUser
import models.common.UserRole
import models.exception.UserNotFoundException
import models.form.FormUserPassword
import play.api.data.Form
import play.api.data.format.Formats._
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Flash
import play.api.mvc.Security
import play.api.mvc.WithHeaders
import play.mvc.Http.Session
import java.security.MessageDigest
import utils.SecurityUtil._
import utils.CommonUtil._
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
import services.CommonService
import utils.CommonUtil._

/**
 * Action to handle the logging section.
 */
trait ABLoginSignup extends TLogin with TSecured { this: Controller => 
  override val logger = generateLogger(this)
  
  val formNewUserMapping = mapping(
      "seqNo" -> optional(of[Int]),
      "username" -> nonEmptyText,
      "name" -> nonEmptyText,
      "gender" -> nonEmptyText,
      "race" -> text.verifying(mustBeEmpty), // Prevent robots from submitting the signup form.
      "idNumber" -> nonEmptyText,
      "address" -> nonEmptyText,
      "dob" -> date("yyyy-MM-dd").verifying(validDOB),
      "userRoleID" ->text,
      "user_role_name" -> optional(text),
      "nationality" -> nonEmptyText,
      "password" -> nonEmptyText,
      "password2" -> nonEmptyText
    )(FormUser.apply)(FormUser.unapply)

  /**
   * Displaying the login page.
   */
  def loginPage = Action { implicit req =>
    logger.debug("ABLoginSignup")
    val username = flash.get("username")
    if (username == None) {
      Ok(views.html.loginsignup(loginForm, Form[FormUser](formNewUserMapping)))
    } else {
      val errorForm = Form(loginForm.mapping, 
          Map("username"->username.get, "password" -> ""), 
          Seq(new FormError("", "Invalid user / password" )), loginForm.value)
      BadRequest (views.html.loginsignup(errorForm, Form[FormUser](formNewUserMapping)))
    }
  }
  
  /**
   * Login authentication.
   */
  def login = Action { implicit req =>
    val tempForm = loginForm.bindFromRequest
    tempForm.fold (
      error => {
        BadRequest(views.html.loginsignup(error, Form[FormUser](formNewUserMapping)))
      },
      data => {
        // Authenticate the user password.
        val validLogin = try {
           	val dbPassword = commonService.getPassword(data.username)
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
            val call = role match {
              case UserRole.ADMIN => routes.ABUserList.listUsers
              case _ => routes.ABLogin.loginPage
            }

            Redirect(call).withSession(Security.username -> formUsername,
            		"menuType" -> role.toString)
          } catch {
            case e: Exception => {
              e.printStackTrace()
              BadRequest(views.html.loginsignup(tempForm, Form[FormUser](formNewUserMapping)))
            }
          }
        } else {
          // Wrong password. Return to login page.
          val errorForm = Form(tempForm.mapping, 
              Map("username"->data.username, "password" -> ""), 
              Seq(new FormError("", "Invalid user / password" )), tempForm.value)
          BadRequest (views.html.loginsignup(errorForm, Form[FormUser](formNewUserMapping)))
        }
      }
    )
  }
  
  def signup() = Action { implicit req =>
    val filledForm = Form[FormUser](formNewUserMapping).bindFromRequest()
    filledForm.fold(
      errorForm => {
        errorForm.errors.foreach{ err =>
          logger.debug(err.key+": "+err.message)
        }
     	val newErrorForm = Form(filledForm.mapping, filledForm.data -- (List("password", "password2")), 
    	      filledForm.errors, filledForm.value)
    	BadRequest (views.html.loginsignup(Form[FormUserPassword](formUserLoginMapping), newErrorForm))
      },
      successForm => {
	      try {
	    	  val dbUser = objUser.findByUserID(successForm.userID.toUpperCase())
	    	  val newErrorForm = Form(filledForm.mapping, 
	    	      filledForm.data -- (List("password", "password2")), 
	    	      Seq(new FormError("username", "This User ID is not available.")), filledForm.value)
	    	  println ("Duplicate userid has been found!")
	    	  BadRequest (views.html.loginsignup(Form[FormUserPassword](formUserLoginMapping), newErrorForm))
	      } catch {
	        case e: UserNotFoundException => {
	        	println ("Ok, valid userid!")
	        	val user = objUser(successForm, true)
	        	val errors:Seq[Option[FormError]] = validatePassword(successForm.password, successForm.password2)
	        	if (errors.size>0) {
	         	  val newErrorForm = Form(filledForm.mapping, filledForm.data -- (List("password", "password2")), 
	        	      errors.map(_.get), filledForm.value)
	        		BadRequest (views.html.loginsignup(Form[FormUserPassword](formUserLoginMapping), newErrorForm))
	        	} else {
	        		val password = successForm.password
	        		commonService.createUserAndPassword(user, password)
	        		Redirect(routes.ABLogin.loginPage()).withSession(session - "mode")
	        	}
	        }
	      }
      }
    )
  }
  
  def jsRoutesUserID = Action { implicit req =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutesUserID")
        (routes.javascript.ABLoginSignup.isUsernameAvailable)).as("text/javascript")
  }
  
  def isUsernameAvailable(pUsername: String) = Action { req =>
    try {
      val dbUser = objUser.findByUserID(pUsername.toUpperCase())
      Ok("false")
    } catch {
      case e: UserNotFoundException => {
        Ok("true")
      }
    }
  }
  
  def jsRoutesIDNumber = Action { implicit req =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutesIDNumber")
        (routes.javascript.ABLoginSignup.isIDNumberAvailable)).as("text/javascript")
  }
  
  def isIDNumberAvailable(idNumber: String) = Action { req =>
    try {
      val dbUser = objUser.findByIDNumber(idNumber.toUpperCase())
      Ok("false")
    } catch {
      case e: UserNotFoundException => {
        Ok("true")
      }
    }
  }
  
}

object ABLoginSignup extends Controller with ABLoginSignup {

}