package controllers

import java.text.SimpleDateFormat
import java.util.Calendar
import models.OBUser
import models.db.DBUser
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc.Action
import play.api.mvc.Controller
import services.DBService
import play.api.data.Forms
import models.form.FormBook
import models.form.FormUser
import controllers.util.MySession
import models.common.DDCountry

/**
 * Action to handle the user section, including the add, update, delete, and view.
 */
object ABUserDetail extends Controller with TSecured {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
  
  val seqCountries = DDCountry.all
  
  val formUserMapping = mapping(
      "rowIdx" -> optional(of[Long]),
      "seqNo" -> optional(of[Long]),
      "userID" -> nonEmptyText,
      "name" -> nonEmptyText,
      "address" -> nonEmptyText,
      "dob" -> date,
      "user_role_id" -> of[Long],
      "user_role_name" -> nonEmptyText
    )(FormUser.apply)(FormUser.unapply)

  val userForm = Form[FormUser](formUserMapping)

  /**
   * Displaying the catalog detail page with blank information.
   */
  def gotoNewUser() = withAuth {username => implicit req =>
    Ok(views.html.user_detail(MODE_ADD, userForm, seqCountries)(session)).withSession(
        session + ("mode" -> MODE_ADD))
  }

  /**
   * Displaying the book detail page with pre-populated user information.
   */
  def edit(pIDStr: String) = TODO
  
  /**
   * Saving the catalog details.
   */
  def save = TODO

}