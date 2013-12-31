package controllers

import models.form.FormUser
import play.api.Play.current
import play.api.cache.Cache
import play.api.data.Form
import play.api.data.Forms.number
import play.api.data.Forms.tuple
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Security
import models.common.UserListItem
import models.common.Gender
import java.text.SimpleDateFormat
import services.CommonService
import utils.CommonUtil._
import utils.Constants._

trait ABUserList extends TSecured { this: Controller => 
    
  val listingForm = Form (
    tuple (
      "currentPageIdx" -> number,
      "maxPageIdx" -> number
    )
  )
  
  /**
   * Displaying the list of users for a corresponding page index.
   */
  def listUsers = withAuth { implicit officerUserID => implicit req => 
    val params = listingForm.bindFromRequest
    val currentPage = Cache.getAs[(List[UserListItem], Int, Int)](CURRENT_PAGE)
    Cache.remove(CURRENT_PAGE)
    val (list1, rowCount, currentPageIdx) = currentPage.getOrElse {
      val res = commonService.partialUsers(1, PAGE_ROW_CNT)
      (res._1, res._2, 1)
    }
    val maxPage = ((rowCount-1) / ITEMS_PER_VIEW)+1
    Ok(views.html.user_list(currentPageIdx, maxPage, 
        list1.map(ABUserList.FormUserListItem(_)))(session))
  }
  
  /**
   * Displaying the first page of the user list.
   */
  def navigateFirst = withAuth { implicit officerUserID => implicit req =>
    val (list1, rowCnt) = commonService.partialUsers(1, PAGE_ROW_CNT)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, 1))
    Redirect(routes.ABUserList.listUsers())
  }
  
  /**
   * Displaying the previous page of the user list.
   */
  def navigatePrev = withAuth { implicit officerUserID => implicit req =>
    val currentPageIdx = req.queryString.get("currentPageIdx").flatMap(_.headOption).get.toInt
    val nextPageIdx = if (currentPageIdx<=1) 1 else currentPageIdx-1
    val startIdx = (nextPageIdx-1)*PAGE_ROW_CNT+1
  	val endIdx = nextPageIdx*PAGE_ROW_CNT
    val (list1, rowCnt) = commonService.partialUsers(startIdx, endIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, nextPageIdx))
    Redirect(routes.ABUserList.listUsers())
  }
  
  /**
   * Displaying the next page of the user list.
   */
  def navigateNext = withAuth { implicit officerUserID => implicit req =>
    val currentPageIdx = req.queryString.get("currentPageIdx").flatMap(_.headOption).get.toInt
    val maxPage = req.queryString.get(MAX_PAGE_IDX).flatMap(_.headOption).get.toInt
    val nextPageIdx = if (currentPageIdx>=maxPage) maxPage else currentPageIdx+1
    logger.debug("maxPage = "+maxPage)
    logger.debug("nextPage = "+nextPageIdx)
    val startIdx = (nextPageIdx-1)*PAGE_ROW_CNT+1
  	val endIdx = nextPageIdx*PAGE_ROW_CNT
    val (list1, rowCnt) = commonService.partialUsers(startIdx, endIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, nextPageIdx))
    Redirect(routes.ABUserList.listUsers())
  }
  
  /**
   * Displaying the last page of the user list.
   */
  def navigateLast = withAuth { implicit officerUserID => implicit req =>
    val maxPage = req.queryString.get(MAX_PAGE_IDX).flatMap(_.headOption).get.toInt
    val startIdx = (maxPage-1)*PAGE_ROW_CNT+1
  	val endIdx = maxPage*PAGE_ROW_CNT
    val (list1, rowCnt) = commonService.partialUsers(startIdx, endIdx)
    Cache.set(CURRENT_PAGE, (list1, rowCnt, maxPage))
    Redirect(routes.ABUserList.listUsers())
  }
  
  /**
   * Removing a particular user from the listing page.
   */
  def removeUser(pSeqNo: String) = withAuth { implicit officerUserID => implicit req => 
    commonService.softDeleteUser(pSeqNo.toInt)
    Redirect(routes.ABUserList.listUsers())
  }
  
  
  def isBlank(str: String) = str==null || str.trim().equals("")
}

object ABUserList extends Controller with ABUserList {
  
  case class FormUserListItem(
    rowIdx: String,
    seqNo: String,
    userID: String, 
    name: String, 
    gender: String,
    race: String,
    idNumber: String, 
    address: String,
    dob: String,
    userRoleName: String,
    nationality: String
  )
  
  object FormUserListItem{
    def apply(pItem: UserListItem): FormUserListItem = {
      FormUserListItem(pItem.idx.get.toString, pItem.seqNo.toString,
          pItem.userID, pItem.name, Gender(pItem.gender).toString, "", pItem.idNumber, pItem.address, 
          sdf.format(pItem.dob), pItem.userRoleName, pItem.nationality)
    }
  }

  val sdf = new SimpleDateFormat("dd-M-yyyy")
}