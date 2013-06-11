package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import models.Book
import views.html.defaultpages.badRequest
import play.api.data.FormError
import play.api.i18n.Messages.Message
import play.api.i18n.Messages
import java.util.Calendar
import java.text.SimpleDateFormat
import scala.collection.mutable.MutableList

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index(Book.all()))
  }
  
  /*def edit(pIDStr: String) = Action { implicit req =>
    val filledForm = bookForm.fill(Book.findByID(pIDStr.toInt))
    Ok(views.html.index(Book.all(), filledForm))
  }*/
  def edit(pIDStr: String) = TODO
  
  def remove(pIDStr: String) = Action { implicit req =>
    Book.delete(pIDStr.toInt);
    Redirect(routes.Application.index())
  }
  
  def isBlank(str: String) = str==null || str.trim().equals("")
}