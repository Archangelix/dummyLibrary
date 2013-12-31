package models.db

import java.util.Date

trait TDBBook {
    def catalogSeqNo: Int 
    def seqNo: Option[Int] 
    def remarks: String 
	def status: String
	def statusUsercode: String
	def statusTimestamp: Date
    def isDeleted: Boolean
    def origin: String 
    def createUsercode: String 
    def createTimestamp: Date
    def auditUsercode: String 
    def auditTimetamp: Date 
    def auditReason: Option[String]
}