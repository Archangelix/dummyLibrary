package models

import java.util.Date
import common.Gender
import db.DBUser
import models.form.FormUser
import java.text.SimpleDateFormat

/**
 * User model business object. This is the object used for all business processes.
 */
case class OBUser (
    rowIdx: Option[Long],
    seqNo: Option[Long], 
    userID: String, 
    name: String, 
    gender: Gender, 
    idNumber: String, 
    address: String, 
    dob: Date,
    role: OBUserRole,
    nationality: Long,
    password: String,
    isDeleted: Boolean
)

object OBUser {
  def apply(pUser: DBUser, pUserRole: OBUserRole): OBUser = 
    OBUser(pUser.rowIdx, pUser.seqNo, pUser.userID, pUser.name, 
        if (pUser.gender) Gender.MALE else Gender.FEMALE, 
        pUser.idNumber, pUser.address, 
        pUser.dob, pUserRole, pUser.nationality, "", pUser.isDeleted)
  
  def apply(pUser: FormUser): OBUser = 
    OBUser(pUser.rowIdx, pUser.seqNo, pUser.userID.toUpperCase, pUser.name, 
        Gender(pUser.gender), 
        pUser.idNumber, pUser.address, 
        sdf.parse(pUser.dob_date+"-"+pUser.dob_month+"-"+pUser.dob_year), 
            OBUserRole(pUser.userRoleID.toInt), pUser.nationality.toInt, "", false)
  
  val sdf = new SimpleDateFormat("dd-MM-yy")
}