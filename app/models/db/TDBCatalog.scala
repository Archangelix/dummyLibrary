package models.db

import java.util.Date

trait TDBCatalog {
    def seqNo: Option[Int]
    def categorySeqNo: Int
    def title: String
    def author: String
    def publishedYear: Int
    def arrivalDate: Date
    def isDeleted: Boolean
    def createUserCode: String
    def createTimestamp: Date
    def auditUserCode: String
    def auditTimestamp: Date
    def auditReason: Option[String]
}