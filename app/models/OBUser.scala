package models

import java.util.Date
import common.Gender
import db.DBUser
import models.form.FormUser
import java.text.SimpleDateFormat
import models.common.UserRole
import services.CommonService

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
)

object OBUser {
  def apply(pUser: DBUser): OBUser = 
    OBUser(Some(pUser.seqNo), pUser.userID, pUser.name, 
        if (pUser.gender) Gender.MALE else Gender.FEMALE, 
        pUser.idNumber, pUser.address, 
        pUser.dob, UserRole(pUser.userRoleSeqNo), pUser.nationality, pUser.isDeleted,
        pUser.createUsercode, pUser.createTimestamp, 
        pUser.auditUsercode, pUser.auditTimestamp, pUser.auditReason
        )
  
  def apply(pUser: FormUser): OBUser = OBUser(pUser, false) 
  
  def apply(pUser: FormUser, pIsUserRegistration: Boolean): OBUser = {
    if (pIsUserRegistration) {
    	OBUser(pUser.seqNo, pUser.userID, pUser.name, 
    			Gender(pUser.gender), 
    			pUser.idNumber, pUser.address, 
    			pUser.dob, 
    			UserRole.BORROWER, pUser.nationality.toInt, false,
    			"", null, "", null, None
    			)
    } else {
    	OBUser(pUser.seqNo, pUser.userID, pUser.name, 
	        Gender(pUser.gender), 
	        pUser.idNumber, pUser.address, 
	        pUser.dob, 
            UserRole(pUser.userRoleID.toInt), pUser.nationality.toInt, false,
            "", null, "", null, None
    		)
    }
  }
 
  def find(pSeqNo: Int): OBUser = {
   CommonService.findUserBySeqNo(pSeqNo) 
  }
  
  def findByUserID(pUserID: String): OBUser = {
	  CommonService.findByUserID(pUserID: String)
  }
  
  def findByIDNumber(pIDNumber: String): OBUser = {
   CommonService.findUserByIDNumber(pIDNumber)
  }
  
  val sdf = new SimpleDateFormat("dd-MM-yyyy")
}