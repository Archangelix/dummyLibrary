package models.common

import java.util.Date

/**
 * User model database object. A business object has to be mapped to / from this database object 
 * in order to communicate with the database.
 */
case class UserListItem (
    idx: Option[Long],
    seqNo: Int,
    userID: String, 
    name: String, 
    address: String, 
    dob: Date,
    gender: Boolean, 
    idNumber: String, 
    nationality: String,
    userRoleName: String)
