package controllers

import models.form.FormBook
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.data.format.Formats._
import java.util.Calendar
import play.api.data.Form
import java.text.SimpleDateFormat
import services.DBService
import models.Book

object BookDetailAction extends Controller {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
  
  val ddItemOriginList = List[(String, String)](
		  ("new" -> "New"),
		  ("old" -> "Old")
      )
    
  val formBookMapping = mapping(
      "idx" -> optional(of[Long]),
      "id" -> optional(of[Long]),
      "catalogID" -> of[Long],
      "origin" -> nonEmptyText,
      "remarks" -> nonEmptyText
    )(FormBook.apply)(FormBook.unapply)

  val bookForm = Form[FormBook] (formBookMapping)

  def newBook(pCatalogID: String) = Action { implicit req => 
    // Flashing works only for redirect.
    // Ok(views.html.book_detail(MODE_EDIT, bookForm, pCatalogID)).flashing("catalogID" -> pCatalogID)
    Ok(views.html.book_detail(MODE_ADD, bookForm, pCatalogID, ddItemOriginList)).withSession(
        session + ("bookMode" -> MODE_ADD)
    )
  }

  def saveNew(pCatalogID: String) = Action { implicit req =>
    val filledForm = bookForm.bindFromRequest
    filledForm.fold(
      error => {
        println("New book failed")
                filledForm.errors.map {err => 
          println("err.key = "+err.key+"; err.message = "+err.message)
        }

        val mode = session.get("bookMode").getOrElse(MODE_ADD)
        BadRequest(views.html.book_detail(mode, error, pCatalogID, ddItemOriginList))
      },
      data => {
        println("New book successful")
        val newBookID = DBService.generateNewBookID(pCatalogID.toInt)
        val newBook = Book(data)
        DBService.createBook(newBook.catalogID, newBookID, newBook.origin, newBook.remarks)
    	Redirect(routes.CatalogDetail.edit(pCatalogID))
      }
    )
  }
  
  def view(pCatalogID: String, pBookID: String) = Action { implicit req => 
    Ok(views.html.book_detail(MODE_ADD, bookForm, pCatalogID, ddItemOriginList))
  }

  def edit(pCatalogID: String, pBookID: String) = Action { implicit req => 
    Ok(views.html.book_detail(MODE_ADD, bookForm, pCatalogID, ddItemOriginList)).withSession(
        session + ("bookMode" -> MODE_EDIT)
    )
  }

  def saveUpdate(pCatalogID: String, pBookID: String) = Action { implicit req => 
    Ok(views.html.book_detail(MODE_ADD, bookForm, pCatalogID, ddItemOriginList))
  }


}