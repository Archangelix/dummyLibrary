package models.db

import java.util.Date

trait TDBTxBorrowHD {
	def seqno: Option[Int]
	def borrowerIDNumber: String
	def borrowTimestamp: Option[Date]
	def officerUsername: String
	def remarks: Option[String]
	def status: String
	def statusUsercode: String
	def statusTimestamp: Date
	def createUsercode: String
	def createTimestamp: Date
	def auditUsercode: String
	def auditTimestamp: Date
	def auditReason: Option[String]
}