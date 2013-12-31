package models

import models.common.DDBookOrigin
import java.util.Date
import models.common.BookStatus

trait TBook {
    def seqNo: Option[Int]
    def catalog: TCatalog
    def origin: DDBookOrigin 
	def status: BookStatus
	def statusUsercode: String
	def statusTimestamp: Date
    def remarks: String 
    def isDeleted: Boolean
    def createUsercode: String 
    def createTimestamp: Date
    def auditUsercode: String
    def auditTimestamp: Date 
    def auditReason: Option[String]

    def activate()(implicit pOfficerUserID: String): TBook
	
	def id(): String
	
	def setAvailable(implicit pOfficerUserID: String): TBook
	
	def artificialCopy(pSeqNo: Option[Int]): TBook
	
	def artificialCopy(pBookOrigin: DDBookOrigin, pRemarks: String): TBook
}