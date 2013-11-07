package models.db

import java.util.Date
import models.OBTxBorrowHD

case class DBTxBorrowHD (
	seqno: Option[Int],
	borrowerIDNumber: String,
	borrowTimestamp: Option[Date],
	officerUsername: String,
	remarks: Option[String],
	status: String,
	statusUsercode: String,
	statusTimestamp: Date,
	createUsercode: String,
	createTimestamp: Date,
	auditUsercode: String,
	auditTimestamp: Date,
	auditReason: Option[String]
)

object DBTxBorrowHD {
  def apply(pObj: OBTxBorrowHD): DBTxBorrowHD = {
    DBTxBorrowHD(
        pObj.seqno,
        pObj.borrower.idNumber,
        pObj.borrowTimestamp,
        pObj.officer.userID,
        pObj.remarks,
        pObj.status.code,
        pObj.statusUsercode,
        pObj.statusTimestamp,
        "", 
        null,
        "",
        null,
        None
    )
  }
}