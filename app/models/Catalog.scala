package models

import java.util.Collection
import models.db.DBCatalog
import models.db.DBBook

/*class Catalog {
  val idx: Option[Long]
  val id: Option[Long] 
  val title: String 
  val author: String 
  val publishedYear: Int 
  val books: Option[Traversable[Book]]
}*/

class Catalog(
  val idx: Option[Long],
  val id: Option[Long],
  val title: String,
  val author: String, 
  val publishedYear: Int, 
  val books: Option[Traversable[Book]]
)

object Catalog {
  def apply(pCatalog: DBCatalog, pBooks: Traversable[DBBook]): Catalog = 
		 new Catalog(pCatalog.idx, pCatalog.id, pCatalog.title, pCatalog.author, pCatalog.publishedYear, 
		     Some(pBooks.map(dbBook => Book(dbBook))))
		    
  def apply(pIdx: Option[Long], pID: Option[Long], pTitle: String, pAuthor: String, pPublishedYear: Int): Catalog = 
		 new Catalog(pIdx, pID, pTitle, pAuthor, pPublishedYear, None)

  def unapply (pCatalog: Catalog) =
    if (pCatalog==null) None
    else Some((pCatalog.idx, pCatalog.id, pCatalog.title, pCatalog.author, pCatalog.publishedYear))
}