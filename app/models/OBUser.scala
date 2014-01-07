package models

import java.util.Date
import common.Gender
import db.DBUser
import models.form.FormUser
import java.text.SimpleDateFormat
import models.common.UserRole
import services.CommonService
import models.db.TDBUser

/**
 * User model business object. This is the object used for all business processes.
 */
case class OBUser (
    seqNo: Option[Int], 
    userID: String, 
    name: String, 
    gender: Gender, 
    idNumber: String, 
    address: String, 
    dob: Date,
    role: UserRole,
    nationality: Int,
    isDeleted: Boolean,
    createUsercode: String, 
    createTimestamp: Date, 
    auditUsercode: String, 
    auditTimestamp: Date, 
    auditReason: Option[String]
) extends TUser

object OBUser extends TCUser {
}