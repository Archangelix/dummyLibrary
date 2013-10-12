package models.form

import models.OBCatalog

/**
 * Catalog form object. A business object has to be mapped to / from this form object 
 * in order to communicate with the Form.
 * 
 * FormCatalog doesn't have the <code>isDeleted</code> attribute. There should never be a situation where
 * a deleted book comes from / to the View layer. 
 * In this layer by default <code>isDeleted</code> attribute is always <code>false</code>.
 */
case class FormCatalog(
  val idx: Option[Long],
  val id: Option[Long],
  val title: String,
  val author: String, 
  val publishedYear: Int,
  val category: Int,
  val books: Option[List[FormBook]]
)

object FormCatalog {
  def apply(pCatalog: OBCatalog): FormCatalog = {
    FormCatalog(pCatalog.idx, pCatalog.id, pCatalog.title, pCatalog.author, 
        pCatalog.publishedYear, pCatalog.category.code, 
        Some(pCatalog.books.get.map(a => FormBook(a))))
  }
}