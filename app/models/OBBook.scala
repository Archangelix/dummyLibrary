package models

import models.db.DBBook
import models.form.FormBook

/**
 * Book business object. This is the object used for all business processes.
 */
case class OBBook (id: Option[Long], catalogID: Long, origin: String, remarks: String, isDeleted: Boolean)

object OBBook {
  def apply(pBook: DBBook): OBBook = OBBook(pBook.id, pBook.catalogID, pBook.origin, pBook.remarks, pBook.isDeleted)
  
  def apply(pBook: FormBook): OBBook = OBBook(pBook.id, pBook.catalogID, pBook.origin, pBook.remarks, false)

}