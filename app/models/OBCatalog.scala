package models

import models.db.DBBook
import models.db.DBCatalog
import models.common.Category
import models.common.CatalogListItem
import services.CommonService
import java.util.Date

/**
 * Catalog business object. This is the object used for all business processes.
 */
case class OBCatalog(
  seqNo: Option[Int],
  title: String,
  author: String, 
  publishedYear: Int, 
  category: Category,
  books: List[OBBook],
  isDeleted: Boolean,
  createUserCode: String, 
  createTimestamp: Date, 
  auditUserCode: String, 
  auditTimestamp: Date, 
  auditReason: Option[String]
)

object OBCatalog {
  def apply(pCatalog: DBCatalog, pBooks: List[DBBook]): OBCatalog = 
		 OBCatalog(pCatalog.seqNo, pCatalog.title, pCatalog.author, 
		     pCatalog.publishedYear, Category(pCatalog.categorySeqNo), pBooks.map(dbBook => OBBook(dbBook)),
		     pCatalog.isDeleted, 
		     pCatalog.createUserCode, pCatalog.createTimestamp,
		     pCatalog.auditUserCode, pCatalog.auditTimestamp, pCatalog.auditReason
		     )

  def find(pCatalogSeqNo: Int)
  		(implicit pIncludeBooks: Boolean = false, 
  		    pIncludeDeletedBook: Boolean = false): OBCatalog = {
      CommonService.findCatalogByID(pCatalogSeqNo)(pIncludeBooks)
  }

  def search(pStr: String): List[CatalogListItem] = {
    val res = CommonService.searchCatalogs(pStr)
    res
  }
  
}