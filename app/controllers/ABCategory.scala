import util.CommonUtil._
import models.form.FormBook
import models.OBBook
import models.OBCatalog
import models.common.DDBookOrigin
import models.common.STATUS_BOOK_AVL

package controllers {

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

object ABCategory extends Controller with TSecured {

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
	  val books: Option[List[FormBook]] = Some(List())
	)
	
	val formCatalogMapping = mapping(
	    "books" -> optional(list(mapping(
		    "idx" -> optional(of[Int]),
		    "seqNo" -> optional(of[Int]),
		    "catalogSeqNo" -> optional(of[Int]),
		    "originCode" -> optional(text),
		    "originDesc" -> optional(text),
		    "status" -> optional(text),
		    "remarks" -> optional(text)
		  )(FormBook.apply)(FormBook.unapply))
	  ))(FormCatalog.apply)(FormCatalog.unapply)
	  
  val catalogForm = Form[FormCatalog](formCatalogMapping)
	  
  /**
   * Displaying the list of users for a corresponding page index.
   */
  def listCategories = withAuth { implicit officerUserID => implicit req => 
    val list1 = Category.alls
    val filledForm = categoryForm.fill(FormCategory(None, None, "", Some(list1)))
    val catalog = OBCatalog.find(1)(true)
    val formCatalog = FormCatalog(Some(catalog.books.map(FormBook(_))))
    val filledCatForm = catalogForm.fill(formCatalog)
    Ok(views.html.categories_list(filledForm, filledCatForm)(session))
  }
  
  def saveCreate = withAuth { implicit officerUserID => implicit req => 
    println("saveCreate Entered!")
    val filledForm = categoryForm.bindFromRequest
    val filledCatForm = catalogForm.bindFromRequest
    filledForm.fold(
        error => {
          println("bad!")
          println("books list = "+filledForm("list"))
          BadRequest(views.html.categories_list(error, filledCatForm)(session))
        }, success => {
        	println("success!")
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
			    	  CommonService.createNewCategory(categoryName)
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
          println("bad!")
          BadRequest(views.html.categories_list(error, filledCatForm)(session))
        }, success => {
          println("success!")
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
				CommonService.updateCategory(selectedSeqNo, categoryName)
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
}