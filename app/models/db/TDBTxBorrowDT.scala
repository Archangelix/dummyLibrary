package models.db

import java.util.Date

trait TDBTxBorrowDT {
	def hdSeqNo: Int
	def catalogSeqNo: Int
	def bookSeqNo: Int
	def status: String
	def statusUsercode: String
	def statusTimestamp: Date
	def createUsercode: String
	def createTimestamp: Date
	def auditUsercode: String
	def auditTimestamp: Date
	def auditReason: Option[String]
}