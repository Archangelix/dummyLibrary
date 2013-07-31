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
import models.OBBook

/**
 * Action to handle the book section, including the add, update, delete, and view.
 */
object ABBookDetail extends Controller with TSecured {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
  
    /**
     * Dropdown items for the 'Origin' type.
     */
  val ddItemOriginList = List[(String, String)](
		  ("new" -> "New"),
		  ("old" -> "Old")
      )
    
  val formBookMapping = mapping(
      "idx" -> optional(of[Long]),
      "id" -> optional(of[Long]),
      "catalogID" -> of[Long],
      "originCode" -> nonEmptyText,
      "originDesc" -> optional(text),
      "remarks" -> nonEmptyText
    )(FormBook.apply)(FormBook.unapply)

  val bookForm = Form[FormBook] (formBookMapping)

  /**
   * Displaying the book detail page with blank information.
   */
  def newBook(pCatalogID: String) = withAuth {username => implicit req => 
    // Flashing works only for redirect.
    // Ok(views.html.book_detail(MODE_EDIT, bookForm, pCatalogID)).flashing("catalogID" -> pCatalogID)
    Ok(views.html.book_detail(MODE_ADD, bookForm, pCatalogID, ddItemOriginList)).withSession(
        session + ("bookMode" -> MODE_ADD)
    )
  }

  /**
   * Saving the details of the new book.
   */
  def saveNew(pCatalogID: String) = withAuth {username => implicit req =>
    val filledForm = bookForm.bindFromRequest
    filledForm.fold(
      error => {
        println("validation failed.")
        val mode = session.get("bookMode").getOrElse(MODE_ADD)
        error.errors.foreach(err => println(err.key + ": "+err.message))
        BadRequest(views.html.book_detail(mode, error, pCatalogID, ddItemOriginList))
      },
      data => {
        println("validation successful.")
        val newBookID = DBService.generateNewBookID(pCatalogID.toInt)
        val newBook = OBBook(data)
        DBService.createBook(newBook.catalogID, newBookID, newBook.originType.code, newBook.remarks)
    	Redirect(routes.ABCatalogDetail.edit(pCatalogID))
      }
    )
  }
  
  /**
   * IN PROGRESS
   * Viewing the book details. The page will be uneditable.
   */
  def view(pCatalogID: String, pBookID: String) = withAuth {username => implicit req => 
    Ok(views.html.book_detail(MODE_ADD, bookForm, pCatalogID, ddItemOriginList))
  }

  /**
   * Displaying the book detail page with pre-populated book information.
   */
  def edit(pCatalogID: String, pBookID: String) = withAuth {username => implicit req => 
    Ok(views.html.book_detail(MODE_ADD, bookForm, pCatalogID, ddItemOriginList)).withSession(
        session + ("bookMode" -> MODE_EDIT)
    )
  }

  /**
   * IN PROGRESS
   * Saving the details of an existing new book.
   */
  def saveUpdate(pCatalogID: String, pBookID: String) = withAuth {username => implicit req => 
    Ok(views.html.book_detail(MODE_ADD, bookForm, pCatalogID, ddItemOriginList))
  }


}