package models.db

import java.util.Date

/**
 * User model database object. A business object has to be mapped to / from this database object 
 * in order to communicate with the database.
 */
case class DBUser (
    seqNo: Option[Long], 
    userID: String, 
    name: String, 
    address: String, 
    dob: Date,
    userRoleID: Long,
    isDeleted: Boolean
)
