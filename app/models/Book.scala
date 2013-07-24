package models

import models.db.DBBook
import models.form.FormBook

case class Book (id: Option[Long], catalogID: Long, origin: String, remarks: String, isDeleted: Boolean)

object Book {
  def apply(pBook: DBBook): Book = Book(pBook.id, pBook.catalogID, pBook.origin, pBook.remarks, pBook.isDeleted)
  
  def apply(pBook: FormBook): Book = Book(pBook.id, pBook.catalogID, pBook.origin, pBook.remarks, false)

}