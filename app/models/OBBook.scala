package models

import utils.CommonUtil.isBlank
import models.common.DDBookOrigin
import models.db.DBBook
import services.CommonService
import java.util.Date
import models.common.BookStatus
import models.common.STATUS_BOOK_PEN
import models.common.STATUS_BOOK_AVL
import models.db.TDBBook
	
/**
 * Book business object. This is the object used for all business processes.
 */
case class OBBook (
    seqNo: Option[Int],
    catalog: TCatalog,
    origin: DDBookOrigin, 
	status: BookStatus,
	statusUsercode: String,
	statusTimestamp: Date,
    remarks: String, 
    isDeleted: Boolean,
    createUsercode: String, 
    createTimestamp: Date, 
    auditUsercode: String, 
    auditTimestamp: Date, 
    auditReason: Option[String]
) extends TBook {
	def activate()(implicit pOfficerUserID: String) = {
	  val now = CommonService.now
	  this.copy(
	      status = STATUS_BOOK_PEN,
	      statusUsercode = pOfficerUserID,
	      statusTimestamp = now,
	      auditUsercode = pOfficerUserID,
	      auditTimestamp = now
	  )
	}
	
	def id():String = {
	  if (seqNo!=None) {
	    catalog.seqNo.get.toString + "." +seqNo.get 
	  } else ""
	}
	
  def setAvailable(implicit pOfficerUserID: String) = {
    val now = CommonService.now
	copy(
	      status = STATUS_BOOK_AVL,
	      statusUsercode = pOfficerUserID,
	      statusTimestamp = now,
	      auditUsercode = pOfficerUserID,
	      auditTimestamp = now
	)
  }
  
  def artificialCopy(pSeqNo: Option[Int]): TBook = copy(seqNo = pSeqNo)
  
  def artificialCopy(pBookOrigin: DDBookOrigin, pRemarks: String): TBook = 
  	copy(origin = pBookOrigin, remarks = pRemarks)

}
    
object OBBook {
  def apply(pBook: TDBBook): TBook = 
    OBBook(
      pBook.seqNo, 
      OBCatalog.find(pBook.catalogSeqNo), 
      DDBookOrigin(pBook.origin), 
      BookStatus(pBook.status),
      pBook.statusUsercode,
      pBook.statusTimestamp,
      pBook.remarks, 
      pBook.isDeleted,
      pBook.createUsercode,
      pBook.createTimestamp,
      pBook.auditUsercode,
      pBook.auditTimetamp,
      pBook.auditReason
    )
  
  def isValidID(pID: String) = {
	 if (!isBlank(pID)) {
	   try {
	     val arr = pID.split('.')
	     arr.length==2
	   } catch {
	     case e: Exception => false;
	   }
	 } else {
	   false
	 }
  }
  
  def find(catalogSeqNo: Int, bookSeqNo: Int) = 
    OBBook(CommonService.findBook(catalogSeqNo, bookSeqNo))

}
