package models

import models.db.DBBook
import models.db.DBCatalog
import models.form.FormBook
import models.form.FormCatalog

/**
 * Catalog business object. This is the object used for all business processes.
 */
class Catalog(
  val idx: Option[Long],
  val id: Option[Long],
  val title: String,
  val author: String, 
  val publishedYear: Int, 
  val isDeleted: Boolean,
  val books: Option[List[Book]]
)

object Catalog {
  def apply(pCatalog: DBCatalog, pBooks: List[DBBook]): Catalog = 
		 new Catalog(pCatalog.idx, pCatalog.id, pCatalog.title, pCatalog.author, 
		     pCatalog.publishedYear, pCatalog.isDeleted, 
		     Some(pBooks.map(dbBook => Book(dbBook))))
		    
  def apply(pIdx: Option[Long], pID: Option[Long], pTitle: String, pAuthor: String, 
      pPublishedYear: Int, pIsDeleted: Boolean): Catalog = 
		 new Catalog(pIdx, pID, pTitle, pAuthor, pPublishedYear, pIsDeleted, None)

  def apply(pIdx: Option[Long], pID: Option[Long], pTitle: String, pAuthor: String, 
      pPublishedYear: Int, pIsDeleted: Boolean, books: List[Book]): Catalog = 
		 new Catalog(pIdx, pID, pTitle, pAuthor, pPublishedYear, pIsDeleted, Some(books))

  def apply(pCatalog: FormCatalog, pBooks: List[FormBook]): Catalog = 
		 new Catalog(pCatalog.idx, pCatalog.id, pCatalog.title, pCatalog.author, 
		     pCatalog.publishedYear, false,  
		     Some(pBooks.map(formBook => Book(formBook))))
		    
  def unapply (pCatalog: Catalog) =
    if (pCatalog==null) None
    else Some((pCatalog.idx, pCatalog.id, pCatalog.title, pCatalog.author, 
        pCatalog.publishedYear, pCatalog.isDeleted))
}