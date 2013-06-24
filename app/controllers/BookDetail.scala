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
      "idx" -> optional(of[Int]),
      "id" -> optional(of[Int]),
      "title" -> nonEmptyText,
      "author" -> nonEmptyText,
      "publishedYear" -> {
        val currentTime = Calendar.getInstance().getTime()
        val yearFormat = new SimpleDateFormat("yyyy")
        val currentYear = yearFormat.format(currentTime).toInt
        number(min = 0, max = currentYear)
      })(Book.apply)(Book.unapply)
    verifying("Duplicate book found.", {
      book => book.id==null || {
        val dbBooks = Book.findDuplicates(book)
        dbBooks.size==0 || dbBooks.size==1 && dbBooks.get(0).id==book.id
      }
    })
  )

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
    val mode = session.get("mode").getOrElse(MODE_ADD)

    tempForm.fold(
      errors => {
        println("Mode = "+mode)
        tempForm.errors.map {err => 
          println(err.message)
        }
        BadRequest(views.html.newbook(mode, tempForm)(session))
      },
      data => {
        if (mode.equals(MODE_ADD)) {
          Book.create(data.title, data.author, data.publishedYear)
        } else {
          Book.update(data.id.get, data.title, data.author, data.publishedYear)
        }
        Redirect(routes.Application.index()).withSession(session - "mode")
      })
  }

}