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
import models.common.BookStatus._
import play.api.data.validation.Constraint
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import play.api.i18n.Messages
import models.TBook
import utils.CommonUtil._
import utils.Constants._

/**
 * Action to handle the book section, including the add, update, delete, and view.
 */
trait ABBookDetail extends TSecured {this: Controller =>
  
  val objBook = OBBook
  val objCatalog = OBCatalog
  
  val ddBookOrigin = DDBookOrigin.all
    /**
     * Dropdown items for the 'Origin' type.
     */
  val ddItemOriginList = List[(String, String)](
    ("new" -> "New"),
    ("old" -> "Old"))
    
	val checkOrigin = (pAllowEmptyOrigin: Boolean) => Constraint[Option[String]]("origin.validation") ( str =>
	  if (!pAllowEmptyOrigin && str==None) {
	    Invalid(Messages("error.required"))
	  } else Valid
	)
	
  val formBookMapping = (allowEmptyOrigin: Boolean) => mapping(
    "idx" -> optional(of[Int]),
    "seqNo" -> optional(of[Int]),
    "catalogSeqNo" -> optional(of[Int]),
    "originCode" -> optional(text).verifying(checkOrigin(allowEmptyOrigin)),
    "originDesc" -> optional(text),
    "status" -> optional(text),
    "remarks" -> optional(text)
  )(ABBookDetail.FormBook.apply)(ABBookDetail.FormBook.unapply)

  val bookForm = (pAllowEmptyOrigin: Boolean) => Form[ABBookDetail.FormBook] (formBookMapping(pAllowEmptyOrigin))

  /**
   * Displaying the book detail page with blank information.
   */
  def newBook(pCatalogSeqNo: String) = withAuth {implicit officerUserID => implicit req => 
    // Flashing works only for redirect.
    // Ok(views.html.book_detail(MODE_EDIT, bookForm, pCatalogID)).flashing("catalogID" -> pCatalogID)
    val newBookForm = bookForm(true).fill(ABBookDetail.FormBook.init(pCatalogSeqNo.toInt))
    Ok(views.html.book_detail(MODE_ADD, newBookForm, ddBookOrigin)).withSession(
        session + ("bookMode" -> MODE_ADD)
    )
  }

  /**
   * Saving the details of the new book.
   */
  def saveNew(pCatalogSeqNo: String) = withAuth { implicit officerUserID => implicit req =>
    val filledForm = bookForm(false).bindFromRequest
    filledForm.fold(
      error => {
        logger.debug("validation failed.")
        val mode = session.get("bookMode").getOrElse(MODE_ADD)
//        error.errors.foreach(err => logger.debug(err.key + ": "+err.message))
        BadRequest(views.html.book_detail(mode, error, ddBookOrigin))
      },
      data => {
        logger.debug("validation successful.")
        val newBook = data.transform()
        commonService.createNewBook(newBook)
    	Redirect(routes.ABCatalogDetail.edit(pCatalogSeqNo))
      }
    )
  }

  /**
   * IN PROGRESS
   * Viewing the book details. The page will be uneditable.
   */
  def view(pCatalogSeqNo: String, pBookSeqNo: String) = withAuth {implicit officerUserID => implicit req => 
    Ok(views.html.book_detail(MODE_ADD, bookForm(true), ddBookOrigin))
  }

  /**
   * Displaying the book detail page with pre-populated book information.
   */
  def edit(pCatalogSeqNo: String, pBookSeqNo: String) = withAuth {implicit officerUserID => implicit req =>
    val dbBook = objBook.find(pCatalogSeqNo.toInt, pBookSeqNo.toInt)
    val newBookForm = bookForm(true).fill(ABBookDetail.FormBook(dbBook))
    Ok(views.html.book_detail(MODE_EDIT, newBookForm, ddBookOrigin)).withSession(
        session + ("bookMode" -> MODE_EDIT)
    )
  }

  /**
   * Saving the details of an existing new book.
   */
  def saveUpdate(pCatalogSeqNo: String, pBookSeqNo: String) = withAuth {implicit officerUserID => implicit req => 
    val filledForm = bookForm(false).bindFromRequest
    filledForm.fold(
      error => {
        logger.debug("validation failed.")
        val mode = session.get("bookMode").getOrElse(MODE_ADD)
//        error.errors.foreach(err => logger.debug(err.key + ": "+err.message))
        BadRequest(views.html.book_detail(mode, error, ddBookOrigin))
      },
      data => {
        logger.debug("validation successfuls.")
        val dbBook = objBook.find(pCatalogSeqNo.toInt, pBookSeqNo.toInt)
        val book = data.merge(dbBook)
        commonService.updateBook(book)
    	Redirect(routes.ABCatalogDetail.edit(pCatalogSeqNo))
      }
    )
  }

  /**
   * Removing a book listed for a certain a catalog.
   */
  def remove(pCatalogID: String, pBookID: String) = withAuth {implicit officerUserID => implicit req => 
    commonService.deleteBook(pCatalogID.toInt, pBookID.toInt)
    Redirect(routes.ABCatalogDetail.edit(pCatalogID))
  }

}

object ABBookDetail extends Controller with ABBookDetail {
  
	case class FormBook(
	    idx: Option[Int], 
	    seqNo: Option[Int], 
	    catalogSeqNo: Option[Int], 
	    originCode: Option[String], 
	    originDesc: Option[String], 
	    status: Option[String],
	    remarks: Option[String]) {
    
	  def merge(pObj: TBook): TBook = {
	    pObj.artificialCopy(
	     DDBookOrigin(this.originCode.get),
	     this.remarks.getOrElse("")
	    )
	  }
	  
	  def transform()(implicit pOfficerUserID: String): TBook = {
	    OBBook(
	      None,
	      objCatalog.find(this.catalogSeqNo.get.toInt), 
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
	  
	  def apply(pBook: TBook): FormBook = {
	    FormBook(None, pBook.seqNo, pBook.catalog.seqNo, Some(pBook.origin.code), 
	        Some(pBook.origin.desc), Some(pBook.status.description), Some(pBook.remarks)
	        )
	  }
	  
	  def init(pCatalogSeqNo: Int): FormBook = {
		FormBook(None, None, Some(pCatalogSeqNo), None, None, Some(STATUS_BOOK_AVL.description), None)	  }
	}

}