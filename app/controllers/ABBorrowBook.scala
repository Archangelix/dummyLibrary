package controllers

import java.util.Date
import models.exception.UserNotFoundException
import play.api.Routes
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc.Controller
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.libs.json.JsNull
import play.api.data.FormError
import models.exception.BookNotFoundException
import models.exception.BookNotAvailableException
import utils.CommonUtil._
import models.TBook
import java.text.SimpleDateFormat
import models.db.DBBook
import models.db.DBCatalog
import models.TCatalog
import models.exception.CatalogNotFoundException
import services.CommonService
import models.TUser
import models.TTxBorrowDT
import models.TTxBorrowHD
import play.api.i18n.Messages
import play.api.mvc.Session
import models.OBTxBorrowHD
import models.TUser
import models.OBUser
import models.OBBook
import utils.CommonUtil._

trait ABBorrowBook extends TSecured { this: Controller =>
  
  val objTxBorrowHD = OBTxBorrowHD
  val objUser = OBUser
  val objBook = OBBook

  val borrowForm = Form[ABBorrowBook.FormBorrow](formBorrowMapping)
  
  val formBookMapping = mapping(
    "bookID" -> optional(text),
    "title" -> optional(text),
    "author" -> optional(text),
    "publishedYear" -> optional(text),
    "category" -> optional(text),
    "remarks" -> optional(text)
  )(ABBorrowBook.BorrowBookItem.apply)(ABBorrowBook.BorrowBookItem.unapply)
  

  val formBorrowMapping = mapping(
	  "msg" -> optional(text),
      "adminUsername" -> optional(text),
      "borrowerID" -> optional(text),
      "borrowerName" -> optional(text),
      "borrowerAddress" -> optional(text),
      "borrowDate" -> optional(text),
      "newBookID" -> optional(text),
      "books" -> optional(list(formBookMapping))
  )(ABBorrowBook.FormBorrow.apply)(ABBorrowBook.FormBorrow.unapply)
  
  val sdf = new SimpleDateFormat("dd-M-yyyy")

  def borrowPage = withAuth { implicit officerUserID => implicit req =>
   val msg = flash.get("msg")
   Ok(views.html.borrow_search_user(borrowForm.fill(
       ABBorrowBook.FormBorrow(msg, None, None, None, None, None, None, None))))
 }
  
 def borrow = withAuth { implicit officerUserID => implicit req =>
   Ok(views.html.borrow_search_user(borrowForm.fill(
       ABBorrowBook.FormBorrow(None, None, None, None, None, None, None, None))))
 }
 
 def javascriptRoutes = withAuth { implicit officerUserID => implicit req =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutes")
        (routes.javascript.ABBorrowBook.fetchUserInfo)).as("text/javascript")
  }
  
  def fetchUserInfo(pUserID: String) = withAuth { implicit officerUserID => implicit req =>
    try {
      logger.debug("fetchUserInfo with userID="+pUserID.toUpperCase())
      val dbUser = objUser.findByUserID(pUserID.toUpperCase())
      val res = JsObject(
	      "name" -> JsString(dbUser.name) ::
	      "address" -> JsString(dbUser.address) ::
	      Nil
	    ) :: Nil
      logger.debug("Found!")
      Ok(Json.toJson(res))
    } catch {
      case e: UserNotFoundException => {
        logger.debug("Not found!")
        Ok(Json.toJson(JsNull))
      }
    }
  }
  
  def validateAndGetBorrower(formBorrower: Form[ABBorrowBook.FormBorrow], pBorrowerID: String)
  	(implicit officerUserID: String):(TUser, Form[ABBorrowBook.FormBorrow]) = {
    if (isBlank(pBorrowerID)) {
	  val newErrorForm = Form(
			  formBorrower.mapping, 
			  formBorrower.data, 
			  Seq(new FormError("borrowerID", Messages("error.required"))), 
			  formBorrower.value)
      (null, newErrorForm)
    } else {
	  try {
    	  val dbUser = objUser.findByIDNumber(pBorrowerID.toUpperCase())
    	  (dbUser, null)
	  } catch {
    	  case e: UserNotFoundException => {
    	    logger.debug("User cannot be found!")
    		val newErrorForm = Form(
    				  formBorrower.mapping, 
    				  formBorrower.data, 
    				  Seq(new FormError("borrowerID", "This User ID cannot be found in the database.")), 
    				  formBorrower.value)
    		(null, newErrorForm)
    	  }
	  }
    }
  }
  
 def searchUser = withAuth { implicit officerUserID => implicit req =>
 	val formBorrower = borrowForm.bindFromRequest()
    formBorrower.fold(
        errorForm => {
            logger.debug("Wrong validation!")
            errorForm.errors.foreach{ err =>
            	logger.debug(err.key+": "+err.message)
            }
            BadRequest(views.html.borrow_search_user(formBorrower))
          },
          data => {
            val borrowerID = data.borrowerID.getOrElse("")
            val (borrower, newErrorForm) = validateAndGetBorrower(formBorrower, borrowerID)
            if (borrower!=null) {
	          Ok(views.html.borrow_search_user(borrowForm.fill(
	        	      ABBorrowBook.FormBorrow(None, Some(officerUserID), 
	        	          Some(borrower.idNumber), Some(borrower.name), Some(borrower.address), 
	        	          Some(sdf.format(new Date())), None, None))))
            } else {
              BadRequest(views.html.borrow_search_user(newErrorForm))
            }
          }
      )
 	}

  def addBook = withAuth { implicit officerUserID => implicit req =>
      logger.debug("Entering addBooks.")
      val formBorrower = borrowForm.bindFromRequest()
      formBorrower.fold(
        errorForm => {
          BadRequest(views.html.borrow_add_book(formBorrower))
        },
        success => {
          try {
            if (isBlank(success.newBookID)) {
              val newErrorForm = Form(
                formBorrower.mapping,
                formBorrower.data,
                Seq(new FormError("newBookID", "This field must be populated!!!")),
                formBorrower.value)
              BadRequest(views.html.borrow_add_book(newErrorForm))
            } else {
              val validBook = objBook.isValidID(success.newBookID.get)
              if (validBook) {
                val transactionID = session.get("transactionID").get.toInt
                val arr = success.newBookID.get.split('.')
                val catalogSeqNo = arr(0).toInt
                val bookSeqNo = arr(1).toInt
                val updatedTx = commonService.addNewBook(transactionID, catalogSeqNo, bookSeqNo)
                val newForm = borrowForm.fill(ABBorrowBook.FormBorrow(updatedTx))
                Ok(views.html.borrow_add_book(newForm))
              } else {
                throw new BookNotFoundException(success.newBookID.get)
              }
            }
          } catch {
            case (BookNotFoundException(_) | CatalogNotFoundException(_)) => {
              val newErrorForm = Form(
                formBorrower.mapping,
                formBorrower.data,
                Seq(new FormError("addError", "This book cannot be found in our system.")),
                formBorrower.value)
              BadRequest(views.html.borrow_add_book(newErrorForm))
            }
            case (BookNotAvailableException(_)) => {
              val newErrorForm = Form(
                formBorrower.mapping,
                formBorrower.data,
                Seq(new FormError("addError", "This book is currently not available.")),
                formBorrower.value)
              BadRequest(views.html.borrow_add_book(newErrorForm))
            }
          }
        })
  }
  
  def viewTransaction(pTransactionSeqNo: String) = withAuth { implicit officerUserID => implicit req =>
    val transaction = commonService.getBorrowTransaction(pTransactionSeqNo.toInt, true)
    val borrower = transaction.borrower
    val newForm = borrowForm.fill(
    	ABBorrowBook.FormBorrow(None, Some(officerUserID), 
          Some(borrower.idNumber), Some(borrower.name), Some(borrower.address), 
          transaction.borrowTimestamp match {
    	  	case Some(x) => Some(sdf.format(x))
    	  	case _ => None
    	  },
          None, 
          Some(List())))
    Ok(views.html.borrow_add_book(newForm))
  }
  
  def startTransaction = withAuth { implicit officerUserID => implicit req =>
    val form = borrowForm.bindFromRequest
    val borrowerID = form("borrowerID").value
    val borrowerName = form("borrowerName").value
    val borrowerAddress = form("borrowerAddress").value
    
    val newTransaction = objTxBorrowHD.generateNewTransaction(borrowerID.get)
    val transactionID = commonService.startTransaction(newTransaction)
    val currentDateStr = sdf.format(new Date())
    val newSession = session + ("transactionID" -> transactionID.toString)
    Redirect(routes.ABBorrowBook.viewTransaction(transactionID.toString)).withSession(newSession)
  }
 
  def showConfirmationPage = withAuth { implicit officerUserID => implicit req =>
    val transactionSeqNo = session.get("transactionID").get.toInt
    val transaction = objTxBorrowHD.find(transactionSeqNo, true)
    val newForm = borrowForm.fill(ABBorrowBook.FormBorrow(transaction))
    Ok(views.html.borrow_confirmation(newForm))
  }
  
  def confirm = withAuth { implicit officerUserID => implicit req =>
    logger.debug("confirm")
    val transactionSeqNo = session.get("transactionID").get.toInt
    val updatedTransaction = commonService.activateBorrowTransaction(transactionSeqNo)
    val newForm = borrowForm.fill(ABBorrowBook.FormBorrow(updatedTransaction))
    Redirect(routes.ABBorrowBook.borrowPage())
    	.flashing("msg" -> "Transaction is successful.")
    	.withSession(session - "transactionID")
  }

}

object ABBorrowBook extends Controller with ABBorrowBook {
  
	case class FormBorrow(
	  val msg: Option[String],
	  val adminUsername: Option[String],
	  val borrowerID: Option[String],
	  val borrowerName: Option[String],
	  val borrowerAddress: Option[String],
	  val borrowDate: Option[String],
	  val newBookID: Option[String],
	  val books: Option[List[BorrowBookItem]]
	)
	
	object FormBorrow {
	  def apply(pObj: TTxBorrowHD, pMessage: Option[String] = None): FormBorrow = {
	    FormBorrow(
	        pMessage,
	        Some(pObj.officer.name),
	        Some(pObj.borrower.idNumber),
	        Some(pObj.borrower.name),
	        Some(pObj.borrower.address),
	        pObj.borrowTimestamp match {
	          case Some(x) => Some(sdf.format(x))
	          case _ => None
	        },
	        None,
	        if (isBlank(pObj.details)) {
	          None 
	        } else {
	          Some(pObj.details.map(BorrowBookItem(_)))
	        }
	    )
	  }
	}
	
	case class BorrowBookItem(bookID: Option[String], title: Option[String], author: Option[String],
      publishedYear: Option[String], category: Option[String], remarks: Option[String])
      
    object BorrowBookItem {
		def apply(pDetail: TTxBorrowDT): BorrowBookItem = {
		  val book = pDetail.book;
		  val catalog = book.catalog
		  BorrowBookItem(
		      Some(catalog.seqNo.get+"."+book.seqNo.get),
		      Some(catalog.title),
		      Some(catalog.author),
		      Some(catalog.publishedYear.toString),
		      Some(catalog.category.toString),
		      Some(book.remarks)
		  )
		}
	}
  
}