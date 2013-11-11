package models.db

import java.util.Date
import models.form.FormCatalog
import models.OBCatalog

/**
 * Catalog database object. A business object has to be mapped to / from this database object 
 * in order to communicate with the database.
 */
case class DBCatalog (
    seqNo: Option[Int], 
    categorySeqNo: Int, 
    title: String, 
    author: String,
    publishedYear: Int, 
    arrivalDate: Date, 
    isDeleted: Boolean, 
    createUserCode: String, 
    createTimestamp: Date, 
    auditUserCode: String, 
    auditTimestamp: Date, 
    auditReason: Option[String]
)

object DBCatalog {
  def apply(pCatalog: OBCatalog): DBCatalog = {
    DBCatalog(pCatalog.seqNo, pCatalog.category.code, 
        pCatalog.title, pCatalog.author, pCatalog.publishedYear.toInt, 
        null, false, 
        pCatalog.createUserCode, pCatalog.createTimestamp,
        pCatalog.auditUserCode, pCatalog.auditTimestamp, pCatalog.auditReason)
  }
}