package controllers

import models.common.Category
import play.api.Play.current
import play.api.cache.Cache
import play.api.data.Form
import play.api.data.Forms.number
import play.api.data.Forms.tuple
import play.api.mvc.Controller
import services.CommonService
import models.OBCatalog
import models.common.CatalogListItem

/**
 * Action to handle the catalog listing section. A catalog can be removed
 * from this listing page.
 */
object ABCatalogList extends Controller with TSecured {
  
  /**
   * COMMON CONSTANTS
   */
  val PAGE_ROW_CNT = 5
  
  case class FormCatalogListItem(
      idx: String,
      seqNo: String,
	  title: String,
	  author: String, 
	  publishedYear: Int,
	  category: String
  )
  
  object FormCatalogListItem {
    def apply(pCatalog: CatalogListItem): FormCatalogListItem = {
      FormCatalogListItem(pCatalog.idx.toString, pCatalog.seqNo.toString, 
          pCatalog.title, pCatalog.author, pCatalog.publishedYear, 
          Category(pCatalog.categorySeqNo).toString)
    }
  }
  
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
  def index = withAuth { implicit officerUserID => implicit req =>
    val params = listingForm.bindFromRequest
    val currentPage = Cache.getAs[(List[CatalogListItem], Int, Int)](CURRENT_PAGE)
    Cache.remove(CURRENT_PAGE)
    val (list1, rowCount, currentPageIdx) = currentPage.getOrElse {
      val res = CommonService.partialCatalogs(1, PAGE_ROW_CNT)
      (res._1, res._2, 1)
    }
    val maxPage = ((rowCount-1) / ITEMS_PER_VIEW)+1
    Ok(views.html.catalog_list(currentPageIdx, maxPage, 
        list1.map(FormCatalogListItem(_)))(session))
  }
  
  /**
   * Displaying the first page of the catalog list.
   */
  def navigateFirst = withAuth { implicit officerUserID => implicit req =>
    val (list1, rowCnt) = CommonService.partialCatalogs(1, PAGE_ROW_CNT)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, 1))
    Redirect(routes.ABCatalogList.index)
  }
  
  /**
   * Displaying the previous page of the catalog list.
   */
  def navigatePrev = withAuth { implicit officerUserID => implicit req =>
    val currentPageIdx = req.queryString.get("currentPageIdx").flatMap(_.headOption).get.toInt
    val nextPageIdx = if (currentPageIdx<=1) 1 else currentPageIdx-1
    val startIdx = (nextPageIdx-1)*PAGE_ROW_CNT+1
  	val endIdx = nextPageIdx*PAGE_ROW_CNT
    val (list1, rowCnt) = CommonService.partialCatalogs(startIdx, endIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, nextPageIdx))
    Redirect(routes.ABCatalogList.index)
  }
  
  /**
   * Displaying the next page of the catalog list.
   */
  def navigateNext = withAuth { implicit officerUserID => implicit req =>
    val currentPageIdx = req.queryString.get("currentPageIdx").flatMap(_.headOption).get.toInt
    val maxPage = req.queryString.get(MAX_PAGE_IDX).flatMap(_.headOption).get.toInt
    val nextPageIdx = if (currentPageIdx>=maxPage) maxPage else currentPageIdx+1
    println("maxPage = "+maxPage)
    println("nextPage = "+nextPageIdx)
  	val startIdx = (maxPage-1)*PAGE_ROW_CNT+1
  	val endIdx = maxPage*PAGE_ROW_CNT
    val (list1, rowCnt) = CommonService.partialCatalogs(startIdx, endIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, nextPageIdx))
    Redirect(routes.ABCatalogList.index)
  }
  
  /**
   * Displaying the last page of the catalog list.
   */
  def navigateLast = withAuth { implicit officerUserID => implicit req =>
    val maxPage = req.queryString.get(MAX_PAGE_IDX).flatMap(_.headOption).get.toInt
  	val startIdx = (maxPage-1)*PAGE_ROW_CNT+1
  	val endIdx = maxPage*PAGE_ROW_CNT
    val (list1, rowCnt) = CommonService.partialCatalogs(startIdx, endIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, maxPage))
    Redirect(routes.ABCatalogList.index)
  }
  
  /**
   * Removing a particular catalog from the listing page.
   */
  def remove(pIDStr: String) = withAuth { implicit officerUserID => implicit req =>
    CommonService.deleteCatalog(pIDStr.toInt)
    Redirect(routes.ABCatalogList.index())
  }
  
  def isBlank(str: String) = str==null || str.trim().equals("")
}