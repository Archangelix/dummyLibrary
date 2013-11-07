package controllers

import models.exception.CategoryNotFoundException
import models.form.FormCategory
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats.longFormat
import play.api.mvc.Controller
import play.api.data.FormError
import util.CommonUtil._
import services.CommonService
import models.common.Category

object ABCategory extends Controller with TSecured {

  val categoryMapping = mapping(
      "selectedID" -> optional(number),
      "updatedCategoryName" -> optional(text),
      "newCategoryName" -> optional(text),
      "list" -> optional(list(
		  mapping(
			  "seqno" -> number,
			  "name" -> text
	      )(Category.apply)(Category.unapply)
      ))
     )(FormCategory.apply)(FormCategory.unapply)
     
  val categoryForm = Form[FormCategory](categoryMapping)
  /**
   * Displaying the list of users for a corresponding page index.
   */
  def listCategories = withAuth { implicit officerUserID => implicit req => 
    val list1 = Category.alls
    val filledForm = categoryForm.fill(FormCategory(None, None, None, Some(list1)))
    Ok(views.html.categories_list(filledForm, list1)(session))
  }
  
  def saveCreate = withAuth { implicit officerUserID => implicit req => 
    println("save Entered!")
    val filledForm = categoryForm.bindFromRequest
    filledForm.fold(
        error => {
          println("bad!")
          BadRequest(views.html.categories_list(error, filledForm.get.list.get)(session))
        }, success => {
        	println("success!")
   		    val categoryName = success.newCategoryName
   		    if (isBlank(categoryName)) {
        	  val newErrors = Form(filledForm.mapping, filledForm.data, 
        	      Seq(new FormError("newCategoryName2", "This is required.")), filledForm.value)
		      BadRequest(views.html.categories_list(newErrors, success.list.get)(session))
   		    } else {
			    try {
			      val category = Category.findByName(categoryName.get)
	        	  val newErrors = Form(filledForm.mapping, filledForm.data, 
	        	      Seq(new FormError("newCategoryName2", "Duplicate category name exists.")), filledForm.value)
			      BadRequest(views.html.categories_list(newErrors, success.list.get)(session))
			    } catch {
			      case e: CategoryNotFoundException => {
			    	  CommonService.createNewCategory(categoryName.get)
			    	  Redirect(routes.ABCategory.listCategories())
			      }
			    }
   		    }
        }
    )
  }
  
  def saveUpdate(pRowIdx: String) = withAuth { implicit officerUserID => implicit req => 
    val filledForm = categoryForm.bindFromRequest
    filledForm.fold(
        error => {
          println("bad!")
          BadRequest(views.html.categories_list(error, filledForm.get.list.get)(session))
        }, success => {
          println("success!")
		  val rowidx = pRowIdx.toInt
		  val categoryName = success.updatedCategoryName.get
		  if (isBlank(categoryName)) {
		    val newErrors = Form(filledForm.mapping, filledForm.data, 
	        	      Seq(new FormError("list["+rowidx+"].name", "This is required.")), filledForm.value)
		    BadRequest(views.html.categories_list(newErrors, success.list.get)(session))
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
	        	      Seq(new FormError("list["+rowidx+"].name", "Duplicate category name exists.")), filledForm.value)
				BadRequest(views.html.categories_list(newErrors, success.list.get)(session))
			  }
		  }
	    }
    )
  }

}