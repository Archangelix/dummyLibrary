package models

import java.util.Date
import models.common.BorrowHDStatus

trait TTxBorrowHD {
	def seqno: Option[Int]
	def borrower: TUser
	def borrowTimestamp: Option[Date]
	def officer: TUser
	def remarks: Option[String]
	def status: BorrowHDStatus
	def statusUsercode: String
	def statusTimestamp: Date
	def details: List[TTxBorrowDT]

	def addBook(catalogSeqNo: Int, bookSeqNo: Int)(implicit pAdminID: String): TTxBorrowHD
	
	def activate()(implicit pOfficerID: String): TTxBorrowHD
	
	def returnBook(pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pOfficerUserID: String): TTxBorrowHD
}