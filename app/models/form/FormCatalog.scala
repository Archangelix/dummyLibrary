package models.form

import models.Catalog

case class FormCatalog(
  val idx: Option[Long],
  val id: Option[Long],
  val title: String,
  val author: String, 
  val publishedYear: Int
)

object FormCatalog {
  def apply(pCatalog: Catalog): FormCatalog = {
    val tempBooks = pCatalog.books.get
    FormCatalog(pCatalog.idx, pCatalog.id, pCatalog.title, pCatalog.author, pCatalog.publishedYear)
  }
}