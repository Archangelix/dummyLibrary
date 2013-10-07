package controllers

import java.text.SimpleDateFormat
import models.OBUser
import models.common.DDCountry
import models.common.DDUserRoles
import models.exception.UserNotFoundException
import models.form.FormUser
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms
import play.api.data.format.Formats._
import play.api.mvc.Action
import play.api.mvc.Controller
import services.DBService
import play.api.data.FormError
import play.api.data.FormError
import play.api.data.validation.Constraint
import java.util.Date
import play.api.data.validation.Invalid
import play.api.data.validation.Valid
import play.api.data.validation.ValidationError
/**
 * Action to handle the user section, including the add, update, delete, and view.
 */
object ABUserDetail extends Controller with TSecured {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
  
  val seqCountries = DDCountry.all
  val seqUserRoles = DDUserRoles.all
  
  def minYear(pMinYear: Int): Constraint[Date] = 
    Constraint[Date]("constraint.minYear", pMinYear) { o => 
      if (o.getYear >= pMinYear) Valid
      else Invalid(ValidationError("error.minYear", pMinYear))
  }
  
  val formEditUserMapping = mapping(
      "rowIdx" -> optional(of[Long]),
      "seqNo" -> optional(of[Long]),
      "userID" -> nonEmptyText,
      "name" -> nonEmptyText,
      "gender" -> nonEmptyText,
      "idNumber" -> nonEmptyText,
      "address" -> nonEmptyText,
      "dob" -> date("dd/MM/yyyy").verifying(minYear(1900)),
      "userRoleID" ->nonEmptyText,
      "user_role_name" -> optional(text),
      "nationality" -> nonEmptyText,
      "password" -> text,
      "password2" -> text
    )(FormUser.apply)(FormUser.unapply)verifying("Invalid date." ,{form =>
      	println("test user detail")
      	/*try {
      	  val date = sdf.parse(form.dob_date+"-"+form.dob_month+"-"+form.dob_year)
      	  true
      	} catch {
      	  case e:Exception => {println("invalid date!"); false}
      	}*/
      	true
  	  })

  val formNewUserMapping = mapping(
      "rowIdx" -> optional(of[Long]),
      "seqNo" -> optional(of[Long]),
      "userID" -> nonEmptyText,
      "name" -> nonEmptyText,
      "gender" -> nonEmptyText,
      "idNumber" -> nonEmptyText,
      "address" -> nonEmptyText,
      "dob" -> date("dd/MM/yyyy").verifying(minYear(1900)),
      "userRoleID" ->nonEmptyText,
      "user_role_name" -> optional(text),
      "nationality" -> nonEmptyText,
      "password" -> nonEmptyText,
      "password2" -> nonEmptyText
    )(FormUser.apply)(FormUser.unapply)verifying("Invalid date." ,{form =>
      	/*try {
      	  val date = sdf.parse(form.dob_date+"-"+form.dob_month+"-"+form.dob_year)
      	  true
      	} catch {
      	  case e:Exception => {println("invalid date!"); false}
      	}*/
      true
  	  })

  val sdf = new SimpleDateFormat("dd-MM-yyyy")
  
  /**
   * Displaying the catalog detail page with blank information.
   */
  def gotoNewUser() = withAuth {username => implicit req =>
    Ok(views.html.user_detail(MODE_ADD, Form[FormUser](formNewUserMapping), 
        seqCountries, seqUserRoles)(session)).withSession(
        session + ("mode" -> MODE_ADD))
  }

  /**
   * Displaying the book detail page with pre-populated user information.
   */
  def edit(pSeqNo: String) = withAuth {username => implicit req =>
    val user= DBService.findUserBySeqNo(pSeqNo.toLong)
    val formUser = FormUser(user)
    val filledForm = Form[FormUser](formEditUserMapping).fill(formUser)
    
    println("filledForm with user userID = "+filledForm("userID").value)
    Ok(views.html.user_detail(MODE_EDIT, filledForm, seqCountries, seqUserRoles)(session)).withSession(
        session + ("mode" -> MODE_EDIT))
  }
  
  def save() = withAuth {username => implicit req =>
    val mode = session.get("mode").getOrElse("")
    val filledForm = 
      if (mode.equals(MODE_ADD)) 
    	Form[FormUser](formNewUserMapping).bindFromRequest() 
      else 
        Form[FormUser](formEditUserMapping).bindFromRequest()
    filledForm.fold(
      errorForm => {
        errorForm.errors.foreach{ err =>
          println(err.key+": "+err.message)
        }
        BadRequest(views.html.user_detail(mode, filledForm, seqCountries, seqUserRoles)(session))
      },
      successForm => {
        if (mode.equals(MODE_ADD)) {
          try {
        	  val dbUser = DBService.findByUserID(successForm.userID.toUpperCase())
        	  val newErrors = Form(filledForm.mapping, filledForm.data, 
        	      Seq(new FormError("userID", "This User ID is not available.")), filledForm.value)
        	  println ("Duplicate userid has been found!")
        	  BadRequest(views.html.user_detail(mode, newErrors, seqCountries, seqUserRoles)(session))
          } catch {
            case e: UserNotFoundException => {
            	println ("Ok, valid userid!")
            	val user = OBUser(successForm)
            	val errors:Seq[Option[FormError]] = validatePassword(successForm.password, successForm.password2)
            	if (errors.size>0) {
	         	  val newErrorForm = Form(filledForm.mapping, filledForm.data, 
	        	      errors.map(_.get), filledForm.value)
            		BadRequest(views.html.user_detail(mode, newErrorForm, seqCountries, seqUserRoles)(session))
            	} else {
            		val password = successForm.password
            		DBService.createUser(user, password)
            		Redirect(routes.ABUserList.listUsers()).withSession(session - "mode")
            	}
            }
          }
        } else {
            val user = OBUser(successForm)
            DBService.updateUser(user)

            val password = successForm.password
            val errors: Seq[Option[FormError]] = if (!isBlank(password)) {
              validatePassword(successForm.password, successForm.password2)
            } else Seq()
            
            if (errors.size > 0) {
              val newErrorForm = Form(filledForm.mapping, filledForm.data,
                errors.map(_.get), filledForm.value)
              BadRequest(views.html.user_detail(mode, newErrorForm, seqCountries, seqUserRoles)(session))
            } else {
              DBService.updatePassword(user.userID, password)
              Redirect(routes.ABUserList.listUsers()).withSession(session - "mode")
            }
          }
      }
    )
  }
  
  def validatePassword(pPassword1: String, pPassword2: String):Seq[Option[FormError]] = {
    Seq(
      if (isBlank(pPassword1)) {
        Some(new FormError("password", "Password is required."))
      } else None,
      if (!isBlank(pPassword1) && !pPassword1.equals(pPassword2)) {
        Some(new FormError("password2", "Both passwords must be the same."))
      } else {
        None
      }
    ).filter(_ != None)
  }
  
  def isBlank(str: String) = str==null || str.trim().equals("")
}