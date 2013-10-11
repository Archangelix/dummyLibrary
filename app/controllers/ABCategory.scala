package controllers

import models.OBCategory
import models.exception.CategoryNotFoundException
import models.form.FormCategory
import play.api.data.Form
import play.api.data.Forms.list
import play.api.data.Forms.mapping
import play.api.data.Forms.of
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.data.format.Formats.longFormat
import play.api.mvc.Controller
import services.DBService
import play.api.data.FormError

object ABCategory extends Controller with TSecured {

  val categoryMapping = mapping(
      "selectedID" -> optional(of[Long]),
      "updatedCategoryName" -> text,
      "newCategoryName" -> text,
      "list" -> optional(list(
		  mapping(
			  "seqno" -> optional(of[Long]),
			  "name" -> text
	      )(OBCategory.apply)(OBCategory.unapply)
      ))
     )(FormCategory.apply)(FormCategory.unapply)
  val categoryForm = Form[FormCategory](categoryMapping)
  /**
   * Displaying the list of users for a corresponding page index.
   */
  def listCategories = withAuth {username => implicit req => 
    val list1 = DBService.listAllCategories()
    val filledForm = categoryForm.fill(FormCategory(None, "", "", Some(list1)))
    Ok(views.html.categories_list(filledForm, list1)(session))
  }
  
  def saveCreate = withAuth {username => implicit req => 
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
        	      Seq(new FormError("newCategoryName", "This is required.")), filledForm.value)
		      BadRequest(views.html.categories_list(newErrors, success.list.get)(session))
   		    } else {
			    try {
			      val OBCategory = DBService.findCategoryByName(categoryName)
	        	  val newErrors = Form(filledForm.mapping, filledForm.data, 
	        	      Seq(new FormError("newCategoryName", "Duplicate category name exists.")), filledForm.value)
			      BadRequest(views.html.categories_list(newErrors, success.list.get)(session))
			    } catch {
			      case e: CategoryNotFoundException => {
			    	  DBService.createCategory(categoryName)
			    	  Redirect(routes.ABCategory.listCategories())
			      }
			    }
   		    }
        }
    )
  }
  
  def saveUpdate(pRowIdx: String) = withAuth {username => implicit req => 
    println("saveUpdate Entered!")
    val filledForm = categoryForm.bindFromRequest
    filledForm.fold(
        error => {
          println("bad!")
          BadRequest(views.html.categories_list(error, filledForm.get.list.get)(session))
        }, success => {
          println("success!")
		  val rowidx = pRowIdx.toLong
		  val selectedSeqNo = success.selectedID
		  val categoryName = success.updatedCategoryName
		  if (isBlank(categoryName)) {
		    val newErrors = Form(filledForm.mapping, filledForm.data, 
	        	      Seq(new FormError("list["+rowidx+"].name", "This is required.")), filledForm.value)
		    BadRequest(views.html.categories_list(newErrors, success.list.get)(session))
		  } else {
			  val isEligibleForUpdate = {
				  try {
					val OBCategory = DBService.findCategoryByName(categoryName)
					OBCategory.seqno==selectedSeqNo
				  } catch {
					case e: CategoryNotFoundException => {
					  true
					}
				  }
	          }
			  if (isEligibleForUpdate) {
				DBService.updateCategory(rowidx, categoryName)
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

  def isBlank(str: String) = str==null || str.trim().equals("")
}