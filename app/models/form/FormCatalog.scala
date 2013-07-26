package models.form

import models.Catalog

/**
 * Catalog form object. A business object has to be mapped to / from this form object 
 * in order to communicate with the Form.
 */
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