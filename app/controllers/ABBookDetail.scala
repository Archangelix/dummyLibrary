package controllers

import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.data.format.Formats._
import java.util.Calendar
import play.api.data.Form
import java.text.SimpleDateFormat
import models.OBBook
import services.CommonService
import models.common.DDBookOrigin
import models.common.STATUS_BOOK_AVL
import models.OBCatalog

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
    ("old" -> "Old"))
    
	case class FormBook(
	    idx: Option[Int], 
	    seqNo: Option[Int], 
	    catalogSeqNo: Option[Int], 
	    originCode: Option[String], 
	    originDesc: Option[String], 
	    status: Option[String],
	    remarks: Option[String]) {
    
	  def merge(pObj: OBBook): OBBook = {
	    pObj.copy(
	     origin = DDBookOrigin(this.originCode.get),
	     remarks = this.remarks.get
	    )
	  }
	  
	  def transform()(implicit pOfficerUserID: String): OBBook = {
	    OBBook(
	      None,
	      OBCatalog.find(this.catalogSeqNo.get.toInt), 
	      DDBookOrigin(this.originCode.get), 
	      STATUS_BOOK_AVL,
	      pOfficerUserID,
	      null,
	      this.remarks.getOrElse(""), 
	      false,
	      pOfficerUserID,
	      null,
	      pOfficerUserID,
	      null,
	      None
	    )
	  }
  	}
	
	object FormBook {
	  def apply(pBook: OBBook): FormBook = {
	    FormBook(None, pBook.seqNo, pBook.catalog.seqNo, Some(pBook.origin.code), 
	        Some(pBook.origin.desc), Some(pBook.status.description), Some(pBook.remarks)
	        )
	  }
	}

  val formBookMapping = mapping(
    "idx" -> optional(of[Int]),
    "seqNo" -> optional(of[Int]),
    "catalogSeqNo" -> optional(of[Int]),
    "originCode" -> optional(text),
    "originDesc" -> optional(text),
    "status" -> optional(text),
    "remarks" -> optional(text)
  )(FormBook.apply)(FormBook.unapply)

  val bookForm = Form[FormBook] (formBookMapping)

  /**
   * Displaying the book detail page with blank information.
   */
  def newBook(pCatalogSeqNo: String) = withAuth {implicit officerUserID => implicit req => 
    // Flashing works only for redirect.
    // Ok(views.html.book_detail(MODE_EDIT, bookForm, pCatalogID)).flashing("catalogID" -> pCatalogID)
    val newBookForm = bookForm.fill(FormBook(None, None, Some(pCatalogSeqNo.toInt), None, None, None, None))
    Ok(views.html.book_detail(MODE_ADD, newBookForm)).withSession(
        session + ("bookMode" -> MODE_ADD)
    )
  }

  /**
   * Saving the details of the new book.
   */
  def saveNew(pCatalogSeqNo: String) = withAuth { implicit officerUserID => implicit req =>
    val filledForm = bookForm.bindFromRequest
    filledForm.fold(
      error => {
        println("validation failed.")
        val mode = session.get("bookMode").getOrElse(MODE_ADD)
//        error.errors.foreach(err => println(err.key + ": "+err.message))
        BadRequest(views.html.book_detail(mode, error))
      },
      data => {
        println("validation successful.")
        val newBook = data.transform()
        CommonService.createNewBook(newBook)
    	Redirect(routes.ABCatalogDetail.edit(pCatalogSeqNo))
      }
    )
  }

  /**
   * IN PROGRESS
   * Viewing the book details. The page will be uneditable.
   */
  def view(pCatalogSeqNo: String, pBookSeqNo: String) = withAuth {implicit officerUserID => implicit req => 
    Ok(views.html.book_detail(MODE_ADD, bookForm))
  }

  /**
   * Displaying the book detail page with pre-populated book information.
   */
  def edit(pCatalogSeqNo: String, pBookSeqNo: String) = withAuth {implicit officerUserID => implicit req =>
    val dbBook = OBBook.find(pCatalogSeqNo.toInt, pBookSeqNo.toInt)
    val newBookForm = bookForm.fill(FormBook(dbBook))
    Ok(views.html.book_detail(MODE_EDIT, newBookForm)).withSession(
        session + ("bookMode" -> MODE_EDIT)
    )
  }

  /**
   * Saving the details of an existing new book.
   */
  def saveUpdate(pCatalogSeqNo: String, pBookSeqNo: String) = withAuth {implicit officerUserID => implicit req => 
    val filledForm = bookForm.bindFromRequest
    filledForm.fold(
      error => {
        println("validation failed.")
        val mode = session.get("bookMode").getOrElse(MODE_ADD)
//        error.errors.foreach(err => println(err.key + ": "+err.message))
        BadRequest(views.html.book_detail(mode, error))
      },
      data => {
        println("validation successfuls.")
        val dbBook = OBBook.find(pCatalogSeqNo.toInt, pBookSeqNo.toInt)
        val book = data.merge(dbBook)
        CommonService.updateBook(book)
    	Redirect(routes.ABCatalogDetail.edit(pCatalogSeqNo))
      }
    )
  }

  /**
   * Removing a book listed for a certain a catalog.
   */
  def remove(pCatalogID: String, pBookID: String) = withAuth {implicit officerUserID => implicit req => 
    CommonService.deleteBook(pCatalogID.toInt, pBookID.toInt)
    Redirect(routes.ABCatalogDetail.edit(pCatalogID))
  }


}