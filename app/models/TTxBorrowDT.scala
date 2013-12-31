package models

import models.common.BorrowDTStatus
import java.util.Date

trait TTxBorrowDT {
	def header: TTxBorrowHD
	def book: TBook
	def status: BorrowDTStatus
	def statusUsercode: String
	def statusTimestamp: Date
	def createUsercode: String
	def createTimestamp: Date
	def auditUsercode: String
	def auditTimestamp: Date
	def auditReason: Option[String]

	def activate()(implicit pOfficerUserID: String): TTxBorrowDT
	
	def returnBook(implicit pOfficerUserID: String): TTxBorrowDT
	
}