package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import models.Book
import views.html.defaultpages.badRequest
import play.api.data.FormError
import play.api.i18n.Messages.Message
import play.api.i18n.Messages
import java.util.Calendar
import java.text.SimpleDateFormat
import scala.collection.mutable.MutableList
import play.api.cache.Cache
import play.api.Play.current

object Application extends Controller {
  
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
  
  def index = Action { implicit req =>
    val params = listingForm.bindFromRequest
    val currentPage = Cache.getAs[(List[Book], Int, Int)](CURRENT_PAGE)
    Cache.remove(CURRENT_PAGE)
    val (list1, rowCount, currentPageIdx) = currentPage.getOrElse {
      val res = Book.partial(1)
      (res._1, res._2, 1)
    }
    val maxPage = ((rowCount-1) / ITEMS_PER_VIEW)+1
    Ok(views.html.index(currentPageIdx, maxPage, list1))
  }
  
  def navigateFirst = Action { implicit req =>
    val (list1, rowCnt) = Book.partial(1)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, 1))
    Redirect(routes.Application.index)
  }
  
  def navigatePrev = Action { implicit req =>
    val currentPageIdx = req.queryString.get("currentPageIdx").flatMap(_.headOption).get.toInt
    val nextPageIdx = if (currentPageIdx<=1) 1 else currentPageIdx-1
    val (list1, rowCnt) = Book.partial(nextPageIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, nextPageIdx))
    Redirect(routes.Application.index)
  }
  
  def navigateNext = Action { implicit req =>
    val currentPageIdx = req.queryString.get("currentPageIdx").flatMap(_.headOption).get.toInt
    val maxPage = req.queryString.get(MAX_PAGE_IDX).flatMap(_.headOption).get.toInt
    val nextPageIdx = if (currentPageIdx>=maxPage) maxPage else currentPageIdx+1
    println("maxPage = "+maxPage)
    println("nextPage = "+nextPageIdx)
    val (list1, rowCnt) = Book.partial(nextPageIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, nextPageIdx))
    Redirect(routes.Application.index)
  }
  
  def navigateLast = Action { implicit req =>
    val maxPage = req.queryString.get(MAX_PAGE_IDX).flatMap(_.headOption).get.toInt
    val (list1, rowCnt) = Book.partial(maxPage)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, maxPage))
    Redirect(routes.Application.index)
  }
  
  def edit(pIDStr: String) = TODO
  
  def remove(pIDStr: String) = Action { implicit req =>
    Book.delete(pIDStr.toInt);
    Redirect(routes.Application.index())
  }
  
  def isBlank(str: String) = str==null || str.trim().equals("")
}