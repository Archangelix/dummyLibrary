package models.db

import java.util.Date
import models.OBTxBorrowDT
import models.TTxBorrowDT

case class DBTxBorrowDT (
	hdSeqNo: Int,
	catalogSeqNo: Int,
	bookSeqNo: Int,
	status: String,
	statusUsercode: String,
	statusTimestamp: Date,
	createUsercode: String,
	createTimestamp: Date,
	auditUsercode: String,
	auditTimestamp: Date,
	auditReason: Option[String]
) extends TDBTxBorrowDT

object DBTxBorrowDT {
  def apply(obj: TTxBorrowDT): DBTxBorrowDT = {
    DBTxBorrowDT(
        obj.header.seqno.get, 
        obj.book.catalog.seqNo.get,
        obj.book.seqNo.get,
        obj.status.code, 
        obj.statusUsercode, 
        obj.statusTimestamp,
        obj.createUsercode,
        obj.createTimestamp,
        obj.auditUsercode,
        obj.auditTimestamp,
        obj.auditReason
        )
  }
}