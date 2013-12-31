package controllers

import java.text.SimpleDateFormat
import java.util.Date
import utils.SecurityUtil._
import utils.CommonUtil._
import models.OBUser
import models.common.DDCountry
import models.common.DDUserRoles
import models.exception.UserNotFoundException
import models.form.FormUser
import play.api.data.Form
import play.api.data.FormError
import play.api.data.Forms._
import play.api.data.format.Formats.longFormat
import play.api.data.validation.Constraint
import play.api.data.validation.Invalid
import play.api.data.validation.Valid
import play.api.data.validation.ValidationError
import play.api.mvc.Controller
import services.CommonService
import java.util.Calendar
import utils.CommonUtil._
import utils.Constants._

/**
 * Action to handle the user section, including the add, update, delete, and view.
 */
trait ABUserDetail extends TSecured { this: Controller => 
  val objUser = OBUser
  
  def minYear(pMinYear: Int): Constraint[Date] =
    Constraint[Date]("constraint.minYear", pMinYear) { o =>
      var cal = Calendar.getInstance();
      cal.setTime(o);
      val year = cal.get(Calendar.YEAR);
      if (year >= pMinYear) Valid
      else Invalid(ValidationError("error.minYear", pMinYear))
    }
  
  val adminUneditable = Constraint[String]("admin.uneditable")(str =>
    if (str.toLowerCase == "admin") Invalid("User admin is uneditable.")
    else Valid
  )
  val formEditUserMapping = mapping(
      "seqNo" -> optional(number),
      "username" -> nonEmptyText.verifying(adminUneditable),
      "name" -> nonEmptyText,
      "gender" -> nonEmptyText,
      "race" -> text.verifying(mustBeEmpty), // Prevent robots from submitting the signup form.
      "idNumber" -> nonEmptyText,
      "address" -> nonEmptyText,
      "dob" -> date("yyyy-MM-dd").verifying(validDOB),
      "userRoleID" ->nonEmptyText,
      "user_role_name" -> optional(text),
      "nationality" -> nonEmptyText,
      "password" -> text,
      "password2" -> text
    )(FormUser.apply)(FormUser.unapply)

  val formNewUserMapping = mapping(
      "seqNo" -> optional(number),
      "username" -> nonEmptyText,
      "name" -> nonEmptyText,
      "gender" -> nonEmptyText,
      "race" -> text.verifying(mustBeEmpty), // Prevent robots from submitting the signup form.
      "idNumber" -> nonEmptyText,
      "address" -> nonEmptyText,
      "dob" -> date("yyyy-MM-dd").verifying(validDOB),
      "userRoleID" ->nonEmptyText,
      "user_role_name" -> optional(text),
      "nationality" -> nonEmptyText,
      "password" -> nonEmptyText,
      "password2" -> nonEmptyText
    )(FormUser.apply)(FormUser.unapply)

  val sdf = new SimpleDateFormat("dd-MM-yyyy")
  
  /**
   * Displaying the catalog detail page with blank information.
   */
  def gotoNewUser() = withAuth { implicit officerUserID => implicit req =>
    val newForm = Form(formNewUserMapping, 
        Map("idNumber" -> req.getQueryString("id").getOrElse("")),
        Seq(), None)
    Ok(views.html.user_detail(MODE_ADD, newForm)(session)).withSession(
        session + ("mode" -> MODE_ADD))
  }

  /**
   * Displaying the book detail page with pre-populated user information.
   */
  def edit(pSeqNo: String) = withAuth { implicit officerUserID => implicit req =>
    val user= objUser.find(pSeqNo.toInt)
    val formUser = FormUser(user)
    val filledForm = Form[FormUser](formEditUserMapping).fill(formUser)
    
    logger.debug("filledForm with user userID = "+filledForm("userID").value)
    Ok(views.html.user_detail(MODE_EDIT, filledForm)(session)).withSession(
        session + ("mode" -> MODE_EDIT))
  }
  
  def saveNew = withAuth { implicit officerUserID => implicit req =>
    val mode = session.get("mode").getOrElse("")
    logger.debug("mode = "+mode)
    val filledForm = Form[FormUser](formNewUserMapping).bindFromRequest() 
    filledForm.fold(
      errorForm => {
        errorForm.errors.foreach{ err =>
          logger.debug(err.key+": "+err.message)
        }
        BadRequest(views.html.user_detail(mode, filledForm)(session))
      },
        successForm => {
          try {
            val dbUser = objUser.findByUserID(successForm.userID.toUpperCase())
            val newErrors = Form(filledForm.mapping, filledForm.data,
              Seq(new FormError("userID", "This User ID is not available.")), filledForm.value)
            logger.debug("Duplicate userid has been found!")
            BadRequest(views.html.user_detail(mode, newErrors)(session))
          } catch {
            case e: UserNotFoundException => {
              logger.debug("Ok, valid userid!")
              val user = objUser(successForm)
              val errors: Seq[Option[FormError]] = validatePassword(successForm.password, successForm.password2)
              if (errors.size > 0) {
                val newErrorForm = Form(filledForm.mapping, filledForm.data,
                  errors.map(_.get), filledForm.value)
                BadRequest(views.html.user_detail(mode, newErrorForm)(session))
              } else {
                val password = successForm.password
                commonService.createUserAndPassword(user, password)
                Redirect(routes.ABUserList.listUsers()).withSession(session - "mode")
              }
            }
          }
        }
    )
  }
  
  def saveUpdate(id: String) = withAuth { implicit officerUserID => implicit req =>
    val mode = session.get("mode").getOrElse("")
    logger.debug("mode = "+mode)
    val filledForm = Form[FormUser](formEditUserMapping).bindFromRequest()
    filledForm.fold(
      errorForm => {
        errorForm.errors.foreach{ err =>
          logger.debug(err.key+": "+err.message)
        }
        BadRequest(views.html.user_detail(mode, filledForm)(session))
      },
        successForm => {
          val user = objUser(successForm)
          commonService.updateUser(user)

          val password = successForm.password
          val errors: Seq[Option[FormError]] = if (!isBlank(password)) {
            validatePassword(successForm.password, successForm.password2)
          } else Seq()

          if (errors.size > 0) {
            val newErrorForm = Form(filledForm.mapping, filledForm.data,
              errors.map(_.get), filledForm.value)
            BadRequest(views.html.user_detail(mode, newErrorForm)(session))
          } else {
            commonService.updatePassword(user.userID, password)
            Redirect(routes.ABUserList.listUsers()).withSession(session - "mode")
          }
        }
    )
  }
  
  def isBlank(str: String) = str==null || str.trim().equals("")
}

object ABUserDetail extends Controller with ABUserDetail {
  
}