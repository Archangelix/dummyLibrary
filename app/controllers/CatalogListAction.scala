package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import models.Catalog
import models.db.DBCatalog
import models.form.FormCatalog
import views.html.defaultpages.badRequest
import play.api.data.FormError
import play.api.i18n.Messages.Message
import play.api.i18n.Messages
import java.util.Calendar
import java.text.SimpleDateFormat
import scala.collection.mutable.MutableList
import play.api.cache.Cache
import play.api.Play.current
import services.DBService

/**
 * Action to handle the catalog listing section. A catalog can be removed
 * from this listing page.
 */
object CatalogListAction extends Controller {
  
  val CURRENT_PAGE = "currentPage"
  val CURRENT_PAGE_IDX = "currentPageIdx"
  val MAX_PAGE_IDX = "maxPageIdx"
  val CURRENT_LIST = "currentList"
  val ITEMS_PER_VIEW = 5 
    
  val listingForm = Form (
    tuple (
      "currentPageIdx" -> number,
      "maxPageIdx" -> number
    )
  )
  
  /**
   * Displaying the list of catalogs for a corresponding page index.
   */
  def index = Action { implicit req =>
    val params = listingForm.bindFromRequest
    val currentPage = Cache.getAs[(List[Catalog], Int, Int)](CURRENT_PAGE)
    Cache.remove(CURRENT_PAGE)
    val (list1, rowCount, currentPageIdx) = currentPage.getOrElse {
      val res = DBService.partialCatalogs(1)
      (res._1, res._2, 1)
    }
    val maxPage = ((rowCount-1) / ITEMS_PER_VIEW)+1
    Ok(views.html.index(currentPageIdx, maxPage, list1.map(catalog => FormCatalog(catalog))))
  }
  
  /**
   * Displaying the first page of the catalog list.
   */
  def navigateFirst = Action { implicit req =>
    val (list1, rowCnt) = DBService.partialCatalogs(1)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, 1))
    Redirect(routes.CatalogListAction.index)
  }
  
  /**
   * Displaying the previous page of the catalog list.
   */
  def navigatePrev = Action { implicit req =>
    val currentPageIdx = req.queryString.get("currentPageIdx").flatMap(_.headOption).get.toInt
    val nextPageIdx = if (currentPageIdx<=1) 1 else currentPageIdx-1
    val (list1, rowCnt) = DBService.partialCatalogs(nextPageIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, nextPageIdx))
    Redirect(routes.CatalogListAction.index)
  }
  
  /**
   * Displaying the next page of the catalog list.
   */
  def navigateNext = Action { implicit req =>
    val currentPageIdx = req.queryString.get("currentPageIdx").flatMap(_.headOption).get.toInt
    val maxPage = req.queryString.get(MAX_PAGE_IDX).flatMap(_.headOption).get.toInt
    val nextPageIdx = if (currentPageIdx>=maxPage) maxPage else currentPageIdx+1
    println("maxPage = "+maxPage)
    println("nextPage = "+nextPageIdx)
    val (list1, rowCnt) = DBService.partialCatalogs(nextPageIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, nextPageIdx))
    Redirect(routes.CatalogListAction.index)
  }
  
  /**
   * Displaying the last page of the catalog list.
   */
  def navigateLast = Action { implicit req =>
    val maxPage = req.queryString.get(MAX_PAGE_IDX).flatMap(_.headOption).get.toInt
    val (list1, rowCnt) = DBService.partialCatalogs(maxPage)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, maxPage))
    Redirect(routes.CatalogListAction.index)
  }
  
  def edit(pIDStr: String) = TODO
  
  /**
   * Removing a particular catalog from the listing page.
   */
  def remove(pIDStr: String) = Action { implicit req =>
    DBService.deleteCatalog(pIDStr.toInt)
    Redirect(routes.CatalogListAction.index())
  }
  
  def isBlank(str: String) = str==null || str.trim().equals("")
}