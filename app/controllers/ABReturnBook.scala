package controllers

import java.util.Date
import models.exception.UserNotFoundException
import play.api.Routes
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.libs.json.JsNull
import play.api.data.FormError
import play.api.mvc.Controller
import models.exception.BookNotFoundException
import utils.CommonUtil._
import java.text.SimpleDateFormat
import models.db.DBBook
import models.db.DBCatalog
import models.exception.CatalogNotFoundException
import services.CommonService
import play.api.data.Mapping
import models.TTxBorrowDT
import models.OBTxBorrowDT
import utils.CommonUtil._

trait ABReturnBook extends TSecured {this: Controller => 
  val objTxBorrowDT = OBTxBorrowDT
  
  val formReturnMapping = 
      mapping(
          "msg" -> optional(text),
		  "transactionID" -> optional(text),
		  "bookID" ->  optional(text) ,
		  "title" -> optional(text),
		  "author" -> optional(text),
		  "category" -> optional(text),
		  "publishedYear" -> optional(text),
		  "borrowDate" -> optional(text),
		  "expReturnDate" -> optional(text),
		  "borrowerID" -> optional(text),
		  "borrowerName" -> optional(text),
		  "borrowerAddress" -> optional(text),
		  "borrowerContactNo" -> optional(text),
		  "officerName" -> optional(text),
	      "bookRemarks" -> optional(text)
	  )(ABReturnBook.FormReturn.apply)(ABReturnBook.FormReturn.unapply)
	  
  val returnForm = Form[ABReturnBook.FormReturn](formReturnMapping)
  
 def returnPage = withAuth { implicit officerUserID => implicit req =>
   val msg = flash.get("msg")
   val dt = returnForm.data
   if (!isBlank(msg)) {
     Ok(views.html.return_search_book(returnForm.copy(
    		 data = returnForm.data + ("msg" -> msg.get)
    		 )))
   } else {
      Ok(views.html.return_search_book(returnForm))
   }
 }
  
  def validateAndGetTxDetail(formReturn: Form[ABReturnBook.FormReturn], pBookID: String):
	  (TTxBorrowDT, Form[ABReturnBook.FormReturn]) = {
    if (isBlank(pBookID)) {
	  val newErrorForm = Form(
			  formReturn.mapping, 
			  formReturn.data, 
			  Seq(new FormError("bookID", Messages("error.required"))), 
			  formReturn.value)
      (null, newErrorForm)
    } else {
	  try {
	      val arr = pBookID.split('.')
	      val catalogSeqNo = arr(0).toInt
	      val bookSeqNo = arr(1).toInt
    	  val dbBook = objTxBorrowDT.findPendingTxDetailByBookID(catalogSeqNo, bookSeqNo)
    	  (dbBook, null)
	  } catch {
    	  case e: Exception => {
    	    e.printStackTrace()
    	    logger.debug("Books cannot be found!")
    		val newErrorForm = Form(
    				  formReturn.mapping, 
    				  formReturn.data, 
    				  Seq(new FormError("bookID", "This book ID cannot be found in the database.")), 
    				  formReturn.value)
    		(null, newErrorForm)
    	  }
	  }
    }
  }
  
 def searchBook = withAuth { implicit officerUserID => implicit req =>
 	val mappedForm = returnForm.bindFromRequest
 	mappedForm.fold(
        errorForm => {
            logger.debug("Wrong validations!")
            errorForm.errors.foreach{ err =>
            	logger.debug(err.key+": "+err.message)
            }
            BadRequest(views.html.return_search_book(mappedForm))
          },
          data => {
            val (txDetail, newErrorForm) = validateAndGetTxDetail(mappedForm, data.bookID.getOrElse(""))
            if (txDetail!=null) {
              val header = txDetail.header
              val borrower = header.borrower
              val book = txDetail.book
              val catalog = book.catalog
              val newForm = ABReturnBook.FormReturn(
                  None,
                  Some(txDetail.header.seqno.get.toString),
                  Some(book.id),
                  Some(catalog.title),
                  Some(catalog.author),
                  Some(catalog.category.name),
                  Some(catalog.publishedYear.toString),
                  Some(sdf.format(header.borrowTimestamp.get)),
                  None,
                  Some(borrower.idNumber),
                  Some(borrower.name),
                  Some(borrower.address),
                  None,
                  Some(header.officer.name),
                  Some(book.remarks)
                  )
              Ok(views.html.return_search_book(returnForm.fill(newForm)))
            } else {
              BadRequest(views.html.return_search_book(newErrorForm))
            }
          }
      )
 	}

 def returnBook = withAuth { implicit officerUserID => implicit req =>
   logger.debug("returnBook")
   val mappedForm = returnForm.bindFromRequest
   mappedForm.fold(
       errorForm => {
         BadRequest(views.html.return_search_book(mappedForm))
       },
       data => {
         val transactionSeqNo = data.transactionID.get.toInt
         commonService.returnBook(transactionSeqNo, data.bookID.get)
         Redirect(routes.ABReturnBook.returnPage)
         	.flashing("msg" -> "The transaction is successful.")
       }
   )
 }
  
  val sdf = new SimpleDateFormat("dd-M-yyyy")
}

object ABReturnBook extends Controller with ABReturnBook {
  
	case class FormReturn(
	  msg: Option[String],
	  transactionID: Option[String],
	  bookID: Option[String],
	  title: Option[String],
	  author: Option[String],
	  category: Option[String],
	  publishedYear: Option[String],
	  borrowDate: Option[String],
	  expReturnDate: Option[String],
	  borrowerID: Option[String],
	  borrowerName: Option[String],
	  borrowerAddress: Option[String],
	  borrowerContactNo: Option[String],
	  officerName: Option[String],
	  bookRemarks: Option[String]
	)
	
}