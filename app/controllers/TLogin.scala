package controllers

import models.OBUser
import models.form.FormUserPassword
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.mvc.Action
import play.api.mvc.Results.Redirect
import services.PSQLService
import utils.CommonUtil.generateLogger
import services.TDBService
import models.TUser
import models.TCUser

trait TLogin {
  val logger = generateLogger(this)
  
  val dbService: TDBService = PSQLService
  val objUser: TCUser = OBUser
  
  val formUserLoginMapping = mapping (
          "username" -> nonEmptyText,
          "password" -> nonEmptyText
      )(FormUserPassword.apply)(FormUserPassword.unapply)
      
  val loginForm = Form[FormUserPassword](formUserLoginMapping)
  
  /**
   * Log out the user. Bring the user back to the login page.
   */
  def logout = Action { implicit req =>
    Redirect(routes.ABLogin.loginPage).withNewSession.flashing("message" -> "Log out successful!")
  }
  
}