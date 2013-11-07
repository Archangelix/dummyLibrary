package models.db

import java.util.Date

import models.OBTxBorrowDT

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
)

object DBTxBorrowDT {
  def apply(obj: OBTxBorrowDT): DBTxBorrowDT = {
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