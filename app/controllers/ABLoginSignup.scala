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
import play.api.mvc.Security
import play.api.mvc.WithHeaders
import play.mvc.Http.Session
import services.DBService
import java.security.MessageDigest
import common.SecurityUtil._
import common.CommonUtil._
import play.api.data.FormError
import controllers.ABUserDetail.MODE_ADD
import controllers.ABUserDetail.MODE_EDIT
import play.api.data.validation.Constraint
import models.form.FormUser
import java.util.Date
import play.api.data.validation.Invalid
import play.api.data.validation.Valid
import play.api.data.validation.ValidationError
import org.joda.time.DateTime
import org.joda.time.Period
import play.api.Routes

/**
 * Action to handle the logging section.
 */
object ABLoginSignup extends Controller with TSecured {

  val formUserLoginMapping = mapping (
          "username" -> nonEmptyText,
          "password" -> nonEmptyText
      )(FormUserPassword.apply)(FormUserPassword.unapply)
      
  val loginForm = Form[FormUserPassword](formUserLoginMapping)
  
  def validDate(): Constraint[Date] = 
    Constraint[Date]("constraint.validDate") {o =>
      println("date constraint!")
      val currentTime = DateTime.now
      val inputTime = new DateTime(o)
      val threshold = inputTime.plus(Period.years(6))
      if (currentTime.compareTo(threshold)<0 ) {
        Invalid(ValidationError("Minimum age is 6 years old.", 6))
      } else {
        Valid
      }
  }
    
  val formNewUserMapping = mapping(
      "rowIdx" -> optional(of[Long]),
      "seqNo" -> optional(of[Long]),
      "username" -> nonEmptyText,
      "name" -> nonEmptyText,
      "gender" -> nonEmptyText,
      "idNumber" -> nonEmptyText,
      "address" -> nonEmptyText,
      "dob" -> date("yyyy-MM-dd").verifying(validDate),
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
    println("ABLoginSignup")
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
           	val dbPassword = DBService.getPassword(data.username.toUpperCase())
        	val words = dbPassword.split('|')
        	println("DB Passwords = "+words)
        	val salt = words(0)
        	val saltedDBPassword = words(1)
        	val encryptedUserPwd = hex_digest(salt+data.password)
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
              Redirect(routes.ABCatalogList.index).withSession(Security.username -> formUsername)
            } else {
              Redirect(routes.ABSearchCatalog.index).withSession(Security.username -> formUsername)
            }
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
          println(err.key+": "+err.message)
        }
     	val newErrorForm = Form(filledForm.mapping, filledForm.data -- (List("password", "password2")), 
    	      filledForm.errors, filledForm.value)
    	BadRequest (views.html.loginsignup(Form[FormUserPassword](formUserLoginMapping), newErrorForm))
      },
      successForm => {
	      try {
	    	  val dbUser = DBService.findByUserID(successForm.userID.toUpperCase())
	    	  val newErrorForm = Form(filledForm.mapping, 
	    	      filledForm.data -- (List("password", "password2")), 
	    	      Seq(new FormError("username", "This User ID is not available.")), filledForm.value)
	    	  println ("Duplicate userid has been found!")
	    	  BadRequest (views.html.loginsignup(Form[FormUserPassword](formUserLoginMapping), newErrorForm))
	      } catch {
	        case e: UserNotFoundException => {
	        	println ("Ok, valid userid!")
	        	val user = OBUser(successForm, true)
	        	val errors:Seq[Option[FormError]] = validatePassword(successForm.password, successForm.password2)
	        	if (errors.size>0) {
	         	  val newErrorForm = Form(filledForm.mapping, filledForm.data -- (List("password", "password2")), 
	        	      errors.map(_.get), filledForm.value)
	        		BadRequest (views.html.loginsignup(Form[FormUserPassword](formUserLoginMapping), newErrorForm))
	        	} else {
	        		val password = successForm.password
	        		DBService.createUser(user, password)
	        		Redirect(routes.ABUserList.listUsers()).withSession(session - "mode")
	        	}
	        }
	      }
      }
    )
  }
  
  def javascriptRoutes = Action { implicit req =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutes")
        (routes.javascript.ABLoginSignup.isUsernameAvailable)).as("text/javascript")
  }
  
  def isUsernameAvailable(pUsername: String) = Action { req =>
    try {
      val dbUser = DBService.findByUserID(pUsername.toUpperCase())
      Ok("false")
    } catch {
      case e: UserNotFoundException => {
        Ok("true")
      }
    }
  }
  
  /**
   * Log out the user. Bring the user back to the login page.
   */
  def logout = Action { implicit req =>
    Redirect(routes.ABLogin.loginPage).withNewSession.flashing("message" -> "Log out successful!")
  }
  
}