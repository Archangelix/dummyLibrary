package models.db

import java.util.Date

trait TDBUser {
    def seqNo: Int
    def userID: String
    def name: String
    def address: String
    def dob: Date
    def gender: Boolean
    def idNumber: String
    def nationality: Int
    def userRoleSeqNo: Int
    def isDeleted: Boolean
    def createUsercode: String
    def createTimestamp: Date
    def auditUsercode: String
    def auditTimestamp: Date
    def auditReason: Option[String]
}