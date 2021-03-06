package controllers

import models.common.UserRole
import models.form.FormSearchCatalog
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Security
import play.api.mvc.Flash
import play.api.mvc.WithHeaders
import play.mvc.Http.Session
import models.common.Category
import models.common.CatalogListItem
import models.OBCatalog
import utils.CommonUtil._

trait ABSearchCatalog extends TSecured with TLogin { this: Controller => 
  override val logger = generateLogger(this)
  
  val objCatalog = OBCatalog

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
//    val filledForm = searchCatalogForm.fill(FormSearchCatalog("", "", "", ""))
    Redirect(routes.ABSearchCatalog.search(req.getQueryString("searchKeyword").getOrElse("")))
//    Ok(views.html.search_catalog(loginForm, "", List()))
  }
  
  def search(pStr: String) = Action { implicit req =>
	val catalogList = objCatalog.search(pStr)
    Ok(views.html.search_catalog(loginForm, pStr, catalogList.map(ABSearchCatalog.FormCatalogListItem(_))))
  }
}

object ABSearchCatalog extends Controller with ABSearchCatalog {
  
  case class FormCatalogListItem(
      idx: String,
	  title: String,
	  author: String, 
	  publishedYear: Int,
	  category: String
  )
  
  object FormCatalogListItem {
    def apply(pCatalog: CatalogListItem): FormCatalogListItem = {
      FormCatalogListItem(pCatalog.idx.toString, pCatalog.title, pCatalog.author, pCatalog.publishedYear, 
          Category(pCatalog.categorySeqNo).toString)
    }
  }
  
}