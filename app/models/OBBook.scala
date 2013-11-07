import util.CommonUtil.isBlank
import models.common.DDBookOrigin
import models.db.DBBook
import services.CommonService
import java.util.Date
import models.common.BookStatus
import models.common.STATUS_BOOK_PEN
import models.common.STATUS_BOOK_AVL

package models{

/**
 * Book business object. This is the object used for all business processes.
 */
case class OBBook (
    seqNo: Option[Int],
    catalog: OBCatalog,
    origin: DDBookOrigin, 
	status: BookStatus,
	statusUsercode: String,
	statusTimestamp: Date,
    remarks: String, 
    isDeleted: Boolean,
    createUsercode: String, 
    createTimestamp: Date, 
    auditUsercode: String, 
    auditTimetamp: Date, 
    auditReason: Option[String]
) {
	def activate()(implicit pOfficerUserID: String) = {
	  val now = CommonService.now
	  OBBook(
	      this.seqNo, 
	      this.catalog, 
	      this.origin, 
	      STATUS_BOOK_PEN,
	      pOfficerUserID,
	      now,
	      this.remarks, 
	      this.isDeleted,
	      this.createUsercode,
	      this.createTimestamp,
	      pOfficerUserID,
	      now,
	      this.auditReason
	  )
	}
	
	def id():String = {
	  if (seqNo!=None) {
	    catalog.seqNo.get.toString + "." +seqNo.get 
	  } else ""
	}
	
  def setAvailable(implicit pOfficerUserID: String) = {
    val now = CommonService.now
	OBBook(
      this.seqNo, 
      this.catalog, 
      this.origin, 
      STATUS_BOOK_AVL,
      pOfficerUserID,
      now,
      this.remarks, 
      this.isDeleted,
      this.createUsercode,
      this.createTimestamp,
      pOfficerUserID,
      now,
      this.auditReason
    )
  }
}
    
object OBBook {
  def apply(pBook: DBBook): OBBook = 
    OBBook(
      Some(pBook.seqNo), 
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
}