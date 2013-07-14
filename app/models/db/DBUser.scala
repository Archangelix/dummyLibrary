package models.db

import java.util.Date

case class DBUser (
    seqNo: Option[Long], 
    userID: String, 
    name: String, 
    address: String, 
    dob: Date,
    userRoleID: Long
)
