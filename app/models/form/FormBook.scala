package models.form

import models.Book

case class FormBook(idx: Option[Long], id: Option[Long], catalogID: Long, origin: String, remarks: String)

object FormBook {
  def apply(pBook: Book): FormBook = {
    FormBook(None, pBook.id, pBook.catalogID, pBook.origin, pBook.remarks)
  }
}