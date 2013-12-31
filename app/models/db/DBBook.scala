package models.db

import java.util.Date
import models.form.FormBook
import models.OBBook
import models.TBook

/**
 * Book database object. A business object has to be mapped to / from this database object 
 * in order to communicate with the database.
 */
case class DBBook (
    catalogSeqNo: Int, 
    seqNo: Option[Int], 
    remarks: String, 
	status: String,
	statusUsercode: String,
	statusTimestamp: Date,
    isDeleted: Boolean, 
    origin: String, 
    createUsercode: String, 
    createTimestamp: Date, 
    auditUsercode: String, 
    auditTimetamp: Date, 
    auditReason: Option[String]
    ) extends TDBBook

object DBBook {
   def apply(pBook: TBook): DBBook = 
     DBBook(
        pBook.catalog.seqNo.get, 
        pBook.seqNo, 
        pBook.remarks, 
        pBook.status.code,
        pBook.statusUsercode,
        pBook.statusTimestamp,
        false, 
        pBook.origin.code, 
        pBook.createUsercode, 
        pBook.createTimestamp,
        "", 
        null, 
        None
   )

}