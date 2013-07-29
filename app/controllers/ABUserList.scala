package controllers

import models.OBUser
import models.form.FormUser
import play.api.Play.current
import play.api.cache.Cache
import play.api.data.Form
import play.api.data.Forms.number
import play.api.data.Forms.tuple
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Security
import services.DBService

object ABUserList extends Controller with TSecured {
  
  val CURRENT_PAGE = "userCurrentPage"
  val CURRENT_PAGE_IDX = "userCurrentPageIdx"
  val MAX_PAGE_IDX = "maxPageIdx"
  val ITEMS_PER_VIEW = 5 
    
  val listingForm = Form (
    tuple (
      "currentPageIdx" -> number,
      "maxPageIdx" -> number
    )
  )
  
  /**
   * Displaying the list of users for a corresponding page index.
   */
  def listUsers = withAuth {username => implicit req => 
    val params = listingForm.bindFromRequest
    val currentPage = Cache.getAs[(List[OBUser], Int, Int)](CURRENT_PAGE)
    Cache.remove(CURRENT_PAGE)
    val (list1, rowCount, currentPageIdx) = currentPage.getOrElse {
      val res = DBService.partialUsers(1)
      (res._1, res._2, 1)
    }
    val maxPage = ((rowCount-1) / ITEMS_PER_VIEW)+1
    Ok(views.html.user_list(currentPageIdx, maxPage, list1.map(user => FormUser(user)))(session))
  }
  
  /**
   * Displaying the first page of the user list.
   */
  def navigateFirst = withAuth {username => implicit req =>
    val (list1, rowCnt) = DBService.partialUsers(1)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, 1))
    Redirect(routes.ABUserList.listUsers())
  }
  
  /**
   * Displaying the previous page of the user list.
   */
  def navigatePrev = withAuth {username => implicit req =>
    val currentPageIdx = req.queryString.get("currentPageIdx").flatMap(_.headOption).get.toInt
    val nextPageIdx = if (currentPageIdx<=1) 1 else currentPageIdx-1
    val (list1, rowCnt) = DBService.partialUsers(nextPageIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, nextPageIdx))
    Redirect(routes.ABUserList.listUsers())
  }
  
  /**
   * Displaying the next page of the user list.
   */
  def navigateNext = withAuth {username => implicit req =>
    val currentPageIdx = req.queryString.get("currentPageIdx").flatMap(_.headOption).get.toInt
    val maxPage = req.queryString.get(MAX_PAGE_IDX).flatMap(_.headOption).get.toInt
    val nextPageIdx = if (currentPageIdx>=maxPage) maxPage else currentPageIdx+1
    println("maxPage = "+maxPage)
    println("nextPage = "+nextPageIdx)
    val (list1, rowCnt) = DBService.partialUsers(nextPageIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, nextPageIdx))
    Redirect(routes.ABUserList.listUsers())
  }
  
  /**
   * Displaying the last page of the user list.
   */
  def navigateLast = withAuth {username => implicit req =>
    val maxPage = req.queryString.get(MAX_PAGE_IDX).flatMap(_.headOption).get.toInt
    val (list1, rowCnt) = DBService.partialUsers(maxPage)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, maxPage))
    Redirect(routes.ABUserList.listUsers())
  }
  
  /**
   * Removing a particular user from the listing page.
   */
  def remove(pIDStr: String) = TODO
  
  def isBlank(str: String) = str==null || str.trim().equals("")
}