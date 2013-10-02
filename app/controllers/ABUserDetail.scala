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
/**
 * Action to handle the user section, including the add, update, delete, and view.
 */
object ABUserDetail extends Controller with TSecured {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
  
  val seqCountries = DDCountry.all
  val seqUserRoles = DDUserRoles.all
  
  val formUserMapping = mapping(
      "rowIdx" -> optional(of[Long]),
      "seqNo" -> optional(of[Long]),
      "userID" -> nonEmptyText,
      "name" -> nonEmptyText,
      "gender" -> nonEmptyText,
      "idNumber" -> nonEmptyText,
      "address" -> nonEmptyText,
      "dob_date" -> text,
      "dob_month" -> text,
      "dob_year" -> text,
      "userRoleID" ->nonEmptyText,
      "user_role_name" -> optional(text),
      "nationality" -> nonEmptyText
    )(FormUser.apply)(FormUser.unapply)verifying("Invalid date." ,{form =>
      	try {
      	  val date = sdf.parse(form.dob_date+"-"+form.dob_month+"-"+form.dob_year)
      	  true
      	} catch {
      	  case e:Exception => {println("invalid date!"); false}
      	}
  	  })

  val userForm = Form[FormUser](formUserMapping)

	val sdf = new SimpleDateFormat("dd-MM-yyyy")
  
  /**
   * Displaying the catalog detail page with blank information.
   */
  def gotoNewUser() = withAuth {username => implicit req =>
    Ok(views.html.user_detail(MODE_ADD, userForm, seqCountries, seqUserRoles)(session)).withSession(
        session + ("mode" -> MODE_ADD))
  }

  /**
   * Displaying the book detail page with pre-populated user information.
   */
  def edit(pSeqNo: String) = withAuth {username => implicit req =>
    val user= DBService.findUserBySeqNo(pSeqNo.toLong)
    val formUser = FormUser(user)
    val filledForm = userForm.fill(formUser)
    
    println("filledForm with user userID = "+filledForm("userID").value)
    Ok(views.html.user_detail(MODE_EDIT, filledForm, seqCountries, seqUserRoles)(session)).withSession(
        session + ("mode" -> MODE_EDIT))
  }
  
  def save() = withAuth {username => implicit req =>
    val mode = session.get("mode").getOrElse("")
    val filledForm = userForm.bindFromRequest()
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
        	  val dbUser = DBService.findByUserID(successForm.userID)
        	  val newErrors = Form(filledForm.mapping, filledForm.data, 
        	      Seq(new FormError("userID","This User ID is not available.")), filledForm.value)
        	  println ("Duplicate userid has been found!")
        	  BadRequest(views.html.user_detail(mode, newErrors, seqCountries, seqUserRoles)(session))
          } catch {
            case e: UserNotFoundException => {
            	println ("Ok, valid userid!")
            	DBService.createUser(OBUser(successForm))
            	Redirect(routes.ABUserList.listUsers()).withSession(session - "mode")
            }
          }
        } else {
          DBService.updateUser(OBUser(successForm))
          Redirect(routes.ABUserList.listUsers()).withSession(session - "mode")
        }
      }
    )
  }
  
}