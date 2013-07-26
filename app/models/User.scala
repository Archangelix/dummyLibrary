package models

import java.util.Date
import models.db.DBUser

/**
 * User model business object. This is the object used for all business processes.
 */
case class User (
    seqNo: Option[Long], 
    userID: String, 
    name: String, 
    address: String, 
    dob: Date,
    userRoleID: Long,
    isDeleted: Boolean
)

object User {
  def apply(pUser: DBUser): User = User(pUser.seqNo, pUser.userID, pUser.name, pUser.address, pUser.dob, pUser.userRoleID,
      pUser.isDeleted)
}