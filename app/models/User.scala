package models

import java.util.Date
import models.db.DBUser

case class User (
    seqNo: Option[Long], 
    userID: String, 
    name: String, 
    address: String, 
    dob: Date,
    userRoleID: Long
)

object User {
  def apply(pUser: DBUser): User = User(pUser.seqNo, pUser.userID, pUser.name, pUser.address, pUser.dob, pUser.userRoleID)
}