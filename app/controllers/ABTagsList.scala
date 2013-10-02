package controllers

import models.OBTag
import models.exception.TagNotFoundException
import models.form.FormTag
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

object ABTagsList extends Controller with TSecured {

  val tagMapping = mapping(
      "selectedID" -> optional(of[Long]),
      "updatedTagName" -> text,
      "newTagName" -> text,
      "list" -> optional(list(
		  mapping(
			  "seqno" -> optional(of[Long]),
			  "name" -> text
	      )(OBTag.apply)(OBTag.unapply)
      ))
     )(FormTag.apply)(FormTag.unapply)
  val tagForm = Form[FormTag](tagMapping)
  /**
   * Displaying the list of users for a corresponding page index.
   */
  def listTags = withAuth {username => implicit req => 
    val list1 = DBService.listAllTags()
    val filledForm = tagForm.fill(FormTag(None, "", "", Some(list1)))
    Ok(views.html.tags_list(filledForm, list1)(session))
  }
  
  def saveCreate = withAuth {username => implicit req => 
    println("save Entered!")
    val filledForm = tagForm.bindFromRequest
    filledForm.fold(
        error => {
          println("bad!")
          BadRequest(views.html.tags_list(error, filledForm.get.list.get)(session))
        }, success => {
        	println("success!")
   		    val tagName = success.newTagName
   		    if (isBlank(tagName)) {
        	  val newErrors = Form(filledForm.mapping, filledForm.data, 
        	      Seq(new FormError("newTagName", "This is required.")), filledForm.value)
		      BadRequest(views.html.tags_list(newErrors, success.list.get)(session))
   		    } else {
			    try {
			      val obTag = DBService.findTagByName(tagName)
	        	  val newErrors = Form(filledForm.mapping, filledForm.data, 
	        	      Seq(new FormError("newTagName", "Duplicate tag name exists.")), filledForm.value)
			      BadRequest(views.html.tags_list(newErrors, success.list.get)(session))
			    } catch {
			      case e: TagNotFoundException => {
			    	  DBService.createTag(tagName)
			    	  Redirect(routes.ABTagsList.listTags())
			      }
			    }
   		    }
        }
    )
  }
  
  def saveUpdate(pRowIdx: String) = withAuth {username => implicit req => 
    println("saveUpdate Entered!")
    val filledForm = tagForm.bindFromRequest
    filledForm.fold(
        error => {
          println("bad!")
          BadRequest(views.html.tags_list(error, filledForm.get.list.get)(session))
        }, success => {
          println("success!")
		  val rowidx = pRowIdx.toLong
		  val selectedSeqNo = success.selectedID
		  val tagName = success.updatedTagName
		  if (isBlank(tagName)) {
		    val newErrors = Form(filledForm.mapping, filledForm.data, 
	        	      Seq(new FormError("list["+rowidx+"].name", "This is required.")), filledForm.value)
		    BadRequest(views.html.tags_list(newErrors, success.list.get)(session))
		  } else {
			  val isEligibleForUpdate = {
				  try {
					val obTag = DBService.findTagByName(tagName)
					obTag.seqno==selectedSeqNo
				  } catch {
					case e: TagNotFoundException => {
					  true
					}
				  }
	          }
			  if (isEligibleForUpdate) {
				DBService.updateTag(rowidx, tagName)
				Redirect(routes.ABTagsList.listTags())
			  } else {
	        	val newErrors = Form(filledForm.mapping, filledForm.data, 
	        	      Seq(new FormError("list["+rowidx+"].name", "Duplicate tag name exists.")), filledForm.value)
				BadRequest(views.html.tags_list(newErrors, success.list.get)(session))
			  }
		  }
	    }
    )
  }

  def isBlank(str: String) = str==null || str.trim().equals("")
}