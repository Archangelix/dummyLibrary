package controllers

import java.text.SimpleDateFormat
import java.util.Calendar
import models.OBCatalog
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.data.Forms
import play.api.data.Field
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.data.validation.Constraint
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import util.CommonUtil._
import services.CommonService
import models.OBBook
import models.common.Category
import models.common.DDBookOrigin
import models.common.STATUS_BOOK_AVL

/**
 * Action to handle the catalog section, including the add, update, delete, and view.
 */
object ABCatalogDetail extends Controller with TSecured {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
  
  val formBookMapping = mapping(
    "idx" -> optional(of[Int]),
    "seqNo" -> optional(of[Int]),
    "catalogSeqNo" -> optional(of[Int]),
    "originCode" -> optional(text),
    "originDesc" -> optional(text),
    "status" -> optional(text),
    "remarks" -> optional(text)
  )(FormBook.apply)(FormBook.unapply)

  val validYear: Constraint[String] = Constraint[String]("valid.year")(str => 
	  if (isBlank(str)) {
	    Invalid(Messages("error.required"))
	  } else {
	    try {
	        val inputYear = str.toInt
	        val currentTime = Calendar.getInstance().getTime()
	        val yearFormat = new SimpleDateFormat("yyyy")
	        val currentYear = yearFormat.format(currentTime).toInt
	    	if ((inputYear<0) || (inputYear >currentYear)) {
	    		Invalid("The input year must be between 0 and current year.")
	    	} else {
	    		Valid
	    	}
	    } catch {
	    	case e: Exception => Invalid(Messages("error.number"))
	    }
	  }
  )
  
  case class FormBook(
	    idx: Option[Int], 
	    seqNo: Option[Int], 
	    catalogSeqNo: Option[Int], 
	    originCode: Option[String], 
	    originDesc: Option[String],
	    status: Option[String],
	    remarks: Option[String]) {
    
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
  
	case class FormCatalog(
	  val seqNo: Option[Int],
	  val title: String,
	  val author: String,
	  val publishedYear: String,
	  val category: String,
	  val books: Option[List[FormBook]] = Some(List())
	) {
	  def transform()(implicit pOfficerUserID: String = ""): OBCatalog = {
	    OBCatalog(
	        this.seqNo,
	        this.title,
	        this.author,
	        this.publishedYear.toInt,
	        Category(this.category.toInt),
	        this.books.getOrElse(List()).map(_.transform),
	        false,
	        "",
	        null,
	        pOfficerUserID,
	        null,
	        None
	    )
	  }
	}
	
	object FormCatalog {
	  def apply(pCatalog: OBCatalog): FormCatalog = {
	    FormCatalog(pCatalog.seqNo,pCatalog.title, pCatalog.author, 
	        pCatalog.publishedYear.toString, pCatalog.category.code.toString, 
	        Some(pCatalog.books.map(FormBook(_))))
	  }
	}

  val formCatalogMapping = mapping(
      "id" -> optional(of[Int]),
      "title" -> nonEmptyText,
      "author" -> nonEmptyText,
      "publishedYear" -> text.verifying(validYear),
      "category" -> nonEmptyText,
      "books" -> optional(list(formBookMapping))
    )(FormCatalog.apply)(FormCatalog.unapply)

  val newCatalogForm = Form[FormCatalog](
    formCatalogMapping verifying ("Duplicate catalog found.", {
      formCatalog =>
        formCatalog.seqNo == None || {
          // There shouldn't be any duplicate catalogs in the database.
          val dbCatalogs = CommonService.findDuplicates(formCatalog.transform)
          dbCatalogs.size == 0 || dbCatalogs.size == 1 && dbCatalogs.get(0).seqNo == formCatalog.seqNo
        }
    }))

  val updateCatalogForm = Form[FormCatalog](formCatalogMapping)
  
  /**
   * Displaying the catalog detail page with blank information.
   */
  def gotoNewCatalog() = withAuth {implicit officerUserID => implicit req =>
    Ok(views.html.catalog_detail(MODE_ADD, newCatalogForm)(session)).withSession(
        session + ("mode" -> MODE_ADD))
  }

  /**
   * Displaying the book detail page with pre-populated catalog information.
   */
  def edit(pIDStr: String) = withAuth {implicit officerUserID => implicit req =>
    println("edit")
    val catalog = OBCatalog.find(pIDStr.toInt)(true)
    val formCatalog = FormCatalog(catalog)
    val filledForm = updateCatalogForm.fill(formCatalog)
    
    val username = session.get("username").getOrElse("")
    Ok(views.html.catalog_detail(MODE_EDIT, filledForm)(session)).withSession(
        session + ("mode" -> MODE_EDIT))
  }
  
  /**
   * Saving the catalog details.
   */
  def saveNew = withAuth {implicit officerUserID => implicit req =>
    println("saveNew")
    val tempForm = newCatalogForm.bindFromRequest()
    val mode = session.get("mode").getOrElse(MODE_ADD)
    val username = session.get("username").getOrElse("")

    tempForm.fold(
      errors => {
        tempForm.errors.map {err => 
          println(err.message)
        }
        BadRequest(views.html.catalog_detail(mode, tempForm)(session))
      },
      data => {
        val catalogSeqNo = CommonService.createNewCatalog(data.transform)
        Redirect(routes.ABBookDetail.newBook(catalogSeqNo.toString)).withSession(session - "mode")
      })
  }

  def saveUpdate = withAuth {implicit officerUserID => implicit req =>
    println("saveUpdate!!!")
    val tempForm = updateCatalogForm.bindFromRequest()
    val mode = session.get("mode").getOrElse(MODE_ADD)
    val username = session.get("username").getOrElse("")

    tempForm.fold(
      errors => {
        tempForm.errors.map {err => 
          println(err.message)
        }
        BadRequest(views.html.catalog_detail(mode, tempForm)(session))
      },
      data => {
        val catalog = data.transform
        CommonService.updateCatalog(catalog)
        Redirect(routes.ABCatalogList.index()).withSession(session - "mode")
      })
  }

}