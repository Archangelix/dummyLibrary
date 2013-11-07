package models

import java.util.Date
import services.CommonService
import models.db.DBTxBorrowHD
import models.db.DBTxBorrowDT
import models.common.BorrowHDStatus
import models.common.STATUS_BORROW_HD_PEN
import models.common.STATUS_BORROW_HD_DFT
import models.common.BorrowHDStatus
import models.common.BorrowHDStatus
import models.common.STATUS_BORROW_HD_COM

class OBTxBorrowHD (
	val seqno: Option[Int],
	val borrower: OBUser,
	val borrowTimestamp: Option[Date],
	val officer: OBUser,
	val remarks: Option[String],
	val status: BorrowHDStatus,
	val statusUsercode: String,
	val statusTimestamp: Date,
	dt: => List[OBTxBorrowDT]
) {
  lazy val details = dt
  
  def addBook(catalogSeqNo: Int, bookSeqNo: Int)(implicit pAdminID: String): OBTxBorrowHD = {
    val book = OBBook.find(catalogSeqNo, bookSeqNo)
    val newOBTxDT = OBTxBorrowDT.init(this, book)

    val newDetails = details match {
      case null => List(newOBTxDT)
      case _ => details ::: List(newOBTxDT)
    }
    
    new OBTxBorrowHD(
        this.seqno, 
        this.borrower,
        this.borrowTimestamp,
        this.officer,
        this.remarks, 
        this.status, 
        this.statusUsercode, 
        this.statusTimestamp,
        newDetails)
  }
  
  def activate()(implicit pOfficerID: String) = {
	val now = CommonService.now
    val details = this.details
    val newDetails = details.map(_.activate) 
    
    new OBTxBorrowHD(
        this.seqno, 
        this.borrower,
        Some(now),
        this.officer,
        this.remarks, 
        STATUS_BORROW_HD_PEN, 
        this.statusUsercode, 
        this.statusTimestamp,
        newDetails)
  }
  
  def returnBook(pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pOfficerUserID: String) = {
    val now = Some(CommonService.now)
    val tempList = details.span(x => x.book.catalog.seqNo.get==pCatalogSeqNo && x.book.seqNo.get==pBookSeqNo)
    val txDetail = tempList._1(0)
    val updatedTxDetail = txDetail.returnBook
    val updatedDetails = updatedTxDetail :: tempList._2
    val unreturnedDetails = tempList._2.filter(x => x.status==STATUS_BORROW_HD_PEN)
    val (updStatus, updStatusUsercode, updStatusTimestamp) = unreturnedDetails.size match {
      case 0 => (STATUS_BORROW_HD_COM, pOfficerUserID, now.get)
      case _ => (this.status, this.statusUsercode, this.statusTimestamp)
    }
    new OBTxBorrowHD(
        this.seqno, 
        this.borrower,
        now,
        this.officer,
        this.remarks, 
        updStatus, 
        updStatusUsercode, 
        updStatusTimestamp,
        updatedDetails)
  }
  
}

object OBTxBorrowHD {
  
  def apply(obj: DBTxBorrowHD, details: List[DBTxBorrowDT]): OBTxBorrowHD = {
    lazy val header: OBTxBorrowHD = 
      new OBTxBorrowHD(obj.seqno, OBUser.findByIDNumber(obj.borrowerIDNumber), obj.borrowTimestamp, 
        OBUser.findByUserID(obj.officerUsername), 
        obj.remarks, BorrowHDStatus(obj.status), obj.statusUsercode, obj.statusTimestamp, 
        obDetails)
    lazy val obDetails: List[OBTxBorrowDT] = details.map((OBTxBorrowDT(_, header)))
    header
  }
 
  def generateNewTransaction(borrowerIDNumber: String)(implicit pOfficerUserID: String) = {
    new OBTxBorrowHD(
        None, 
        OBUser.findByIDNumber(borrowerIDNumber),
        null,
        OBUser.findByUserID(pOfficerUserID),
        None, 
        STATUS_BORROW_HD_DFT, 
        pOfficerUserID, 
        null,
        List())
  }
  
  def find(pTransactionSeqNo: Int, pIncludeDetails: Boolean): OBTxBorrowHD = {
    CommonService.getBorrowTransaction(pTransactionSeqNo, pIncludeDetails)
  }
  
}