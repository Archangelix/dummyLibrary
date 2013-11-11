package models

import java.util.Date
import models.db.DBTxBorrowDT
import models.common.BorrowDTStatus
import services.CommonService
import models.common.STATUS_BORROW_DT_PEN
import models.common.STATUS_BORROW_DT_DFT
import models.common.STATUS_BORROW_DT_RET

class OBTxBorrowDT (
	hd: => OBTxBorrowHD,
	val book: OBBook,
	val status: BorrowDTStatus,
	val statusUsercode: String,
	val statusTimestamp: Date,
	val createUsercode: String,
	val createTimestamp: Date,
	val auditUsercode: String,
	val auditTimestamp: Date,
	val auditReason: Option[String]) {
	
  lazy val header = hd
  
	def activate()(implicit pOfficerUserID: String) = {
	  val now = CommonService.now()
      val updatedBook = this.book.activate

      new OBTxBorrowDT(
    	this.header,
    	updatedBook,
    	STATUS_BORROW_DT_PEN,
    	pOfficerUserID,
    	now,
    	this.createUsercode,
    	this.createTimestamp,
    	"",
    	null,
    	None
      )
	}

  def returnBook(implicit pOfficerUserID: String) = {
	  val now = CommonService.now()
      val updatedBook = this.book.setAvailable

      new OBTxBorrowDT(
    	this.header,
    	updatedBook,
    	STATUS_BORROW_DT_RET,
    	pOfficerUserID,
    	now,
    	this.createUsercode,
    	this.createTimestamp,
    	"",
    	null,
    	None
      )
  }
}

object OBTxBorrowDT {
      
  def apply(obj: DBTxBorrowDT, pBorrowHD: OBTxBorrowHD): OBTxBorrowDT = {
    new OBTxBorrowDT(
        pBorrowHD, 
        OBBook.find(obj.catalogSeqNo, obj.bookSeqNo), 
        BorrowDTStatus(obj.status), 
        obj.statusUsercode, 
        obj.statusTimestamp,
        obj.createUsercode,
        obj.createTimestamp,
        obj.auditUsercode,
        obj.auditTimestamp,
        obj.auditReason
        )
  }
  
  def init(pBorrowHD: OBTxBorrowHD, pBook: OBBook)(implicit pOfficerID: String) = {
    new OBTxBorrowDT(
        pBorrowHD, 
        pBook, 
        STATUS_BORROW_DT_DFT, 
        pOfficerID, 
        null,
        pOfficerID,
        null,
        pOfficerID,
        null,
        None
       )
  }
  
  def findPendingTxDetailByBookID(pCatalogSeqNo: Int, pBookSeqNo: Int) = {
    CommonService.findPendingTxByBookID(pCatalogSeqNo, pBookSeqNo)
  }
}