package models

import models.db.DBBook
import models.form.FormBook
import models.common.DDBookOriginType

/**
 * Book business object. This is the object used for all business processes.
 */
case class OBBook (id: Option[Long], catalogID: Long, originType: DDBookOriginType, remarks: String, isDeleted: Boolean)

object OBBook {
  def apply(pBook: DBBook): OBBook = OBBook(pBook.id, pBook.catalogID, 
      DDBookOriginType(pBook.originCode), pBook.remarks, pBook.isDeleted)
  
  def apply(pBook: FormBook): OBBook = OBBook(pBook.id, pBook.catalogID, DDBookOriginType(pBook.originCode), pBook.remarks, false)

}