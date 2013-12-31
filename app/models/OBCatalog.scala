package models

import models.db.DBBook
import models.db.DBCatalog
import models.common.Category
import models.common.CatalogListItem
import services.CommonService
import java.util.Date
import models.db.TDBCatalog
import models.db.TDBBook

/**
 * Catalog business object. This is the object used for all business processes.
 */
case class OBCatalog(
  seqNo: Option[Int],
  title: String,
  author: String, 
  publishedYear: Int, 
  category: Category,
  books: List[TBook],
  isDeleted: Boolean,
  createUserCode: String, 
  createTimestamp: Date, 
  auditUserCode: String, 
  auditTimestamp: Date, 
  auditReason: Option[String]
) extends TCatalog

object OBCatalog {
  val objBook = OBBook
  
  def apply(pCatalog: TDBCatalog, pBooks: List[TDBBook]): TCatalog = 
		 OBCatalog(pCatalog.seqNo, pCatalog.title, pCatalog.author, 
		     pCatalog.publishedYear, Category(pCatalog.categorySeqNo), pBooks.map(dbBook => objBook(dbBook)),
		     pCatalog.isDeleted, 
		     pCatalog.createUserCode, pCatalog.createTimestamp,
		     pCatalog.auditUserCode, pCatalog.auditTimestamp, pCatalog.auditReason
		     )

  def find(pCatalogSeqNo: Int)
  		(implicit pIncludeBooks: Boolean = false, 
  		    pIncludeDeletedBook: Boolean = false): TCatalog = {
      CommonService.findCatalogByID(pCatalogSeqNo)(pIncludeBooks)
  }

  def search(pStr: String): List[CatalogListItem] = {
    val res = CommonService.searchCatalogs(pStr)
    res
  }
  
}