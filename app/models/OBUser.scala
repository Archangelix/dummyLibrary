package models

import java.util.Date
import models.db.DBUser

/**
 * User model business object. This is the object used for all business processes.
 */
case class OBUser (
    seqNo: Option[Long], 
    userID: String, 
    name: String, 
    address: String, 
    dob: Date,
    userRoleID: Long,
    isDeleted: Boolean
)

object OBUser {
  def apply(pUser: DBUser): OBUser = OBUser(pUser.seqNo, pUser.userID, pUser.name, pUser.address, pUser.dob, pUser.userRoleID,
      pUser.isDeleted)
}