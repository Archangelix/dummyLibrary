package models

import models.db.DBBook
import models.db.DBCatalog
import models.form.FormBook
import models.form.FormCatalog

/**
 * Catalog business object. This is the object used for all business processes.
 */
class OBCatalog(
  val idx: Option[Long],
  val id: Option[Long],
  val title: String,
  val author: String, 
  val publishedYear: Int, 
  val isDeleted: Boolean,
  val books: Option[List[OBBook]]
)

object OBCatalog {
  def apply(pCatalog: DBCatalog, pBooks: List[DBBook]): OBCatalog = 
		 new OBCatalog(pCatalog.idx, pCatalog.id, pCatalog.title, pCatalog.author, 
		     pCatalog.publishedYear, pCatalog.isDeleted, 
		     Some(pBooks.map(dbBook => OBBook(dbBook))))
		    
  def apply(pIdx: Option[Long], pID: Option[Long], pTitle: String, pAuthor: String, 
      pPublishedYear: Int, pIsDeleted: Boolean): OBCatalog = 
		 new OBCatalog(pIdx, pID, pTitle, pAuthor, pPublishedYear, pIsDeleted, None)

  def apply(pIdx: Option[Long], pID: Option[Long], pTitle: String, pAuthor: String, 
      pPublishedYear: Int, pIsDeleted: Boolean, books: List[OBBook]): OBCatalog = 
		 new OBCatalog(pIdx, pID, pTitle, pAuthor, pPublishedYear, pIsDeleted, Some(books))

  def apply(pCatalog: FormCatalog, pBooks: List[FormBook]): OBCatalog = 
		 new OBCatalog(pCatalog.idx, pCatalog.id, pCatalog.title, pCatalog.author, 
		     pCatalog.publishedYear, false,  
		     Some(pBooks.map(formBook => OBBook(formBook))))
		    
  def unapply (pCatalog: OBCatalog) =
    if (pCatalog==null) None
    else Some((pCatalog.idx, pCatalog.id, pCatalog.title, pCatalog.author, 
        pCatalog.publishedYear, pCatalog.isDeleted))
}