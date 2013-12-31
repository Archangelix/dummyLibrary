package controllers

import utils.CommonUtil._
import models.form.FormBook
import models.OBBook
import models.OBCatalog
import models.common.DDBookOrigin
import models.common.STATUS_BOOK_AVL
import models.TBook

import models.exception.CategoryNotFoundException
import models.form.FormCategory
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats.longFormat
import play.api.mvc.Controller
import play.api.data.FormError
import services.CommonService
import models.common.Category
import play.api.data.format.Formats._
import utils.CommonUtil._
import utils.Constants._

trait ABCategory extends TSecured { this: Controller => 
  val objCatalog = OBCatalog
  val objBook = OBBook
  
  val categoryMapping = mapping(
      "selectedID" -> optional(number),
      "updatedCategoryName" -> optional(text),
      "newCategoryName" -> nonEmptyText,
      "list" -> optional(list(
		  mapping(
			  "seqno" -> number,
			  "name" -> text
	      )(Category.apply)(Category.unapply)
      ))
     )(FormCategory.apply)(FormCategory.unapply)
     
  val categoryForm = Form[FormCategory](categoryMapping)

	val formCatalogMapping = mapping(
	    "books" -> optional(list(mapping(
		    "idx" -> optional(of[Int]),
		    "seqNo" -> optional(of[Int]),
		    "catalogSeqNo" -> optional(of[Int]),
		    "originCode" -> optional(text),
		    "originDesc" -> optional(text),
		    "status" -> optional(text),
		    "remarks" -> optional(text)
		  )(ABCategory.FormBook.apply)(ABCategory.FormBook.unapply))
	  ))(ABCategory.FormCatalog.apply)(ABCategory.FormCatalog.unapply)
	  
  val catalogForm = Form[ABCategory.FormCatalog](formCatalogMapping)
	  
  /**
   * Displaying the list of users for a corresponding page index.
   */
  def listCategories = withAuth { implicit officerUserID => implicit req => 
    val list1 = Category.alls
    val filledForm = categoryForm.fill(FormCategory(None, None, "", Some(list1)))
    val catalog = objCatalog.find(1)(true)
    val formCatalog = ABCategory.FormCatalog(Some(catalog.books.map(ABCategory.FormBook(_))))
    val filledCatForm = catalogForm.fill(formCatalog)
    Ok(views.html.categories_list(filledForm, filledCatForm)(session))
  }
  
  def saveCreate = withAuth { implicit officerUserID => implicit req => 
    logger.debug("saveCreate Entered!")
    val filledForm = categoryForm.bindFromRequest
    val filledCatForm = catalogForm.bindFromRequest
    filledForm.fold(
        error => {
          logger.debug("bad!")
          logger.debug("books list = "+filledForm("list"))
          BadRequest(views.html.categories_list(error, filledCatForm)(session))
        }, success => {
        	logger.debug("success!")
   		    val categoryName = success.newCategoryName
   		    if (isBlank(categoryName)) {
        	  val newErrors = Form(filledForm.mapping, filledForm.data, 
        	      Seq(new FormError("newCategoryName2", "This is required.")), filledForm.value)
		      BadRequest(views.html.categories_list(newErrors, filledCatForm)(session))
   		    } else {
			    try {
			      val category = Category.findByName(categoryName)
	        	  val newErrors = Form(filledForm.mapping, filledForm.data, 
	        	      Seq(new FormError("newCategoryName2", "Duplicate category name exists.")), filledForm.value)
			      BadRequest(views.html.categories_list(newErrors, filledCatForm)(session))
			    } catch {
			      case e: CategoryNotFoundException => {
			    	  commonService.createNewCategory(categoryName)
			    	  Redirect(routes.ABCategory.listCategories())
			      }
			    }
   		    }
        }
    )
  }
  
  def saveUpdate(pSeqNo: String) = withAuth { implicit officerUserID => implicit req => 
    val filledForm = categoryForm.bindFromRequest
    val filledCatForm = catalogForm.bindFromRequest
    filledForm.fold(
        error => {
          logger.debug("bad!")
          BadRequest(views.html.categories_list(error, filledCatForm)(session))
        }, success => {
          logger.debug("success!")
		  val rowIdx = success.list.get.map(_.code).zipWithIndex.filter(_._1==pSeqNo.toInt)(0)._2
		  val categoryName = success.updatedCategoryName.getOrElse("")
		  if (isBlank(categoryName)) {
		    val newErrors = Form(filledForm.mapping, filledForm.data, 
	        	      Seq(new FormError("list["+rowIdx+"].name", "This is required.")), filledForm.value)
		    BadRequest(views.html.categories_list(newErrors, filledCatForm)(session))
		  } else {
			  val selectedSeqNo = success.selectedID.get
			  val isEligibleForUpdate = {
				  try {
					val category = Category.findByName(categoryName)
					category.code==selectedSeqNo
				  } catch {
					case e: CategoryNotFoundException => {
					  true
					}
				  }
	          }
			  if (isEligibleForUpdate) {
				commonService.updateCategory(selectedSeqNo, categoryName)
				Redirect(routes.ABCategory.listCategories())
			  } else {
	        	val newErrors = Form(filledForm.mapping, filledForm.data, 
	        	      Seq(new FormError("list["+pSeqNo+"].name", "Duplicate category name exists.")), filledForm.value)
				BadRequest(views.html.categories_list(newErrors, filledCatForm)(session))
			  }
		  }
	    }
    )
  }

}

object ABCategory extends Controller with ABCategory {
  
  case class FormBook(
	    idx: Option[Int], 
	    seqNo: Option[Int], 
	    catalogSeqNo: Option[Int], 
	    originCode: Option[String], 
	    originDesc: Option[String],
	    status: Option[String],
	    remarks: Option[String]) {
    
	  def transform()(implicit pOfficerUserID: String): TBook = {
	    objBook(
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
	}
  
  	case class FormCatalog(
	  val books: Option[List[FormBook]] = Some(List())
	)
	
}