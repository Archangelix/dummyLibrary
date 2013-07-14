package models

import models.db.DBBook

case class Book (id: Option[Long], catalogID: Long, remarks: String, isDeleted: Boolean)

object Book {
  def apply(pBook: DBBook): Book = Book(pBook.id, pBook.catalogID, pBook.remarks, pBook.isDeleted)
}