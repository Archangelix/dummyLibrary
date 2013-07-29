package models

import java.util.Date
import models.db.DBUser

/**
 * User model business object. This is the object used for all business processes.
 */
case class OBUser (
    rowIdx: Long,
    seqNo: Option[Long], 
    userID: String, 
    name: String, 
    address: String, 
    dob: Date,
    role: OBUserRole,
    isDeleted: Boolean
)

object OBUser {
  def apply(pUser: DBUser, pUserRole: OBUserRole): OBUser = 
    OBUser(pUser.rowIdx, pUser.seqNo, pUser.userID, pUser.name, pUser.address, 
        pUser.dob, pUserRole, pUser.isDeleted)
}