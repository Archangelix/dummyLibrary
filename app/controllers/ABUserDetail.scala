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

/**
 * Action to handle the user section, including the add, update, delete, and view.
 */
object ABUserDetail extends Controller with TSecured {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
  
  val formUserMapping = mapping(
      "rowIdx" -> of[Long],
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
   * Displaying the user detail page with blank information.
   */
  def gotoNewUser() = TODO

  /**
   * Displaying the book detail page with pre-populated user information.
   */
  def edit(pIDStr: String) = TODO
  
  /**
   * Saving the user details.
   */
  def save = TODO

}