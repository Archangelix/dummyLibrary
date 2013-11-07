package models.db

import java.util.Date
import models.OBUser
import models.common.Gender

/**
 * User model database object. A business object has to be mapped to / from this database object 
 * in order to communicate with the database.
 */
case class DBUser (
    seqNo: Int,
    userID: String, 
    name: String, 
    address: String, 
    dob: Date,
    gender: Boolean, 
    idNumber: String, 
    nationality: Int,
    userRoleSeqNo: Int,
    isDeleted: Boolean,
    createUsercode: String, 
    createTimestamp: Date, 
    auditUsercode: String, 
    auditTimestamp: Date, 
    auditReason: Option[String]
)

object DBUser {
  def apply(pUser: OBUser): DBUser = {
    DBUser(pUser.seqNo.getOrElse(0), pUser.userID, pUser.name, pUser.address, pUser.dob, 
        pUser.gender==Gender.MALE, pUser.idNumber, pUser.nationality, 
        pUser.role.seqNo, pUser.isDeleted, 
        pUser.createUsercode, pUser.createTimestamp, 
        pUser.auditUsercode, pUser.auditTimestamp, pUser.auditReason)
  }
}