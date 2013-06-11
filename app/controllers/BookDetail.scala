package controllers

import java.text.SimpleDateFormat
import java.util.Calendar

import models.Book
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc.Action
import play.api.mvc.Controller

object BookDetail extends Controller {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
    
  val bookForm = Form[Book](
    mapping(
      "id" -> optional(of[Int]),
      "title" -> nonEmptyText,
      "author" -> nonEmptyText,
      "publishedYear" -> {
        val currentTime = Calendar.getInstance().getTime()
        val yearFormat = new SimpleDateFormat("yyyy")
        val currentYear = yearFormat.format(currentTime).toInt
        number(min = 0, max = currentYear)
      })(Book.apply)(Book.unapply))

  def gotoNewBook() = Action { implicit req =>
    Ok(views.html.newbook(MODE_ADD, bookForm)(session)).withSession(
        session + ("mode" -> MODE_ADD))
  }

  def edit(pIDStr: String) = Action { implicit req =>
    val filledForm = bookForm.fill(Book.findByID(pIDStr.toInt))
    Ok(views.html.newbook(MODE_EDIT, filledForm)(session)).withSession(
        session + ("mode" -> MODE_EDIT))
  }
  
  def save = Action { implicit req =>
    val tempForm = bookForm.bindFromRequest()

    tempForm.fold(
      errors => {
        val mode = session.get("mode").getOrElse(MODE_ADD)
        println("Mode = "+mode)
        BadRequest(views.html.newbook(mode, tempForm)(session))
      },
      data => {
        Book.create(data.title, data.author, data.publishedYear)
        Redirect(routes.Application.index()).withSession(session - "mode")
      })
  }

}