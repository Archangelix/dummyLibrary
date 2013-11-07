package models.form

import models.OBCatalog
import models.db.DBCatalog
import models.db.DBBook

/**
 * Catalog form object. A business object has to be mapped to / from this form object 
 * in order to communicate with the Form.
 * 
 * FormCatalog doesn't have the <code>isDeleted</code> attribute. There should never be a situation where
 * a deleted book comes from / to the View layer. 
 * In this layer by default <code>isDeleted</code> attribute is always <code>false</code>.
 */
case class FormCatalog(
  val seqNo: Option[Int],
  val title: String,
  val author: String,
  val publishedYear: String,
  val category: String,
  val books: Option[List[FormBook]]
)

object FormCatalog {
  def apply(pCatalog: OBCatalog): FormCatalog = {
    FormCatalog(pCatalog.seqNo,pCatalog.title, pCatalog.author, 
        pCatalog.publishedYear.toString, pCatalog.category.code.toString, 
        Some(pCatalog.books.map(FormBook(_))))
  }
}