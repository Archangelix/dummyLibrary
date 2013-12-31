package models

import models.common.UserRole
import models.common.Gender
import java.util.Date

trait TUser {
    def seqNo: Option[Int] 
    def userID: String 
    def name: String 
    def gender: Gender 
    def idNumber: String 
    def address: String
    def dob: Date
    def role: UserRole
    def nationality: Int
    def isDeleted: Boolean
    def createUsercode: String 
    def createTimestamp: Date
    def auditUsercode: String 
    def auditTimestamp: Date
    def auditReason: Option[String]
}