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
      "searchKeyword" -> text,
      "author" -> text,
      "title" -> text,
      "caseSensitive" -> text
    )(FormSearchCatalog.apply)(FormSearchCatalog.unapply)

  val searchCatalogForm = Form[FormSearchCatalog] (formSearchCatalogMapping)
  
  /**
   * Displaying the login page.
   */
  def index = Action { implicit req =>
    val filledForm = searchCatalogForm.fill(FormSearchCatalog("abc", "", "", ""))
    Ok(views.html.search_catalog("", List()))
  }
  
  def search(pStr: String) = Action { implicit req =>
    	val catalogList = DBService.findCatalogs(pStr);
        Ok(views.html.search_catalog(pStr, catalogList.map(FormCatalog(_))))
    /*val cat = searchCatalogForm.bindFromRequest
    cat.fold(
      error => {
        println("errors")
        BadRequest(views.html.search_catalog(pStr, error, List()))
      },
      success => {
        println("success")
        val casesensitive = "Y".equals(success.caseSensitive)
   		//println("Case sensitive = "+success.caseSensitive)
    	val catalogList = DBService.findCatalogs(success, casesensitive);
        Ok(views.html.search_catalog(pStr, cat, catalogList.map(FormCatalog(_))))
      }
    )*/
  }
}