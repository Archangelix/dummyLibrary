package models.form

import models.Book

/**
 * Book form object. A business object has to be mapped to / from this form object 
 * in order to communicate with the Form.
 */
case class FormBook(idx: Option[Long], id: Option[Long], catalogID: Long, origin: String, remarks: String)

object FormBook {
  def apply(pBook: Book): FormBook = {
    FormBook(None, pBook.id, pBook.catalogID, pBook.origin, pBook.remarks)
  }
}