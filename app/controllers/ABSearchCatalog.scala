package controllers

import models.OBUserRole
import models.form.FormSearchCatalog
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Security
import play.api.mvc.Flash
import play.api.mvc.WithHeaders
import play.mvc.Http.Session
import services.DBService
import models.form.FormCatalog

object ABSearchCatalog extends Controller with TSecured {
  
  val formSearchCatalogMapping = mapping(
      "author" -> text,
      "title" -> text,
      "caseSensitive" -> text
    )(FormSearchCatalog.apply)(FormSearchCatalog.unapply)

  val searchCatalogForm = Form[FormSearchCatalog] (formSearchCatalogMapping)
  
  /**
   * Displaying the login page.
   */
  def index = Action { implicit req =>
    val filledForm = searchCatalogForm.fill(FormSearchCatalog("", "", ""))
    Ok(views.html.borrower.searchBook(filledForm, List()))
  }
  
  def search(pStr: String) = Action { implicit req =>
    val cat = searchCatalogForm.bindFromRequest
    cat.fold(
      error => {
        BadRequest(views.html.borrower.searchBook(error, List()))
      },
      success => {
        val casesensitive = "Y".equals(success.caseSensitive)
   		//println("Case sensitive = "+success.caseSensitive)
    	val catalogList = DBService.findCatalogs(success, casesensitive);
        Ok(views.html.borrower.searchBook(cat, catalogList.map(FormCatalog(_))))
      }
    )
  }
}