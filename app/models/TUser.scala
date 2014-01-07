package models

import models.common.UserRole
import models.common.Gender
import java.util.Date
import services.TCommonService
import java.text.SimpleDateFormat
import services.CommonService
import models.db.TDBUser
import models.form.FormUser

trait TUser {
    def seqNo: Option[Int] 
    def userID: String 
    def name: String 
    def gender: Gender 
    def idNumber: String 
    def address: String
    def dob: Date
    def role: UserRole
    def nationality: Int
    def isDeleted: Boolean
    def createUsercode: String 
    def createTimestamp: Date
    def auditUsercode: String 
    def auditTimestamp: Date
    def auditReason: Option[String]
}

trait TCUser {
  val commonService: TCommonService = CommonService
  
  def apply(pUser: TDBUser): TUser =
    OBUser(Some(pUser.seqNo), pUser.userID, pUser.name,
      if (pUser.gender) Gender.MALE else Gender.FEMALE,
      pUser.idNumber, pUser.address,
      pUser.dob, UserRole(pUser.userRoleSeqNo), pUser.nationality, pUser.isDeleted,
      pUser.createUsercode, pUser.createTimestamp,
      pUser.auditUsercode, pUser.auditTimestamp, pUser.auditReason)

  def apply(pUser: FormUser): TUser = OBUser(pUser, false)

  def apply(pUser: FormUser, pIsUserRegistration: Boolean): TUser = {
    if (pIsUserRegistration) {
      OBUser(pUser.seqNo, pUser.userID, pUser.name,
        Gender(pUser.gender),
        pUser.idNumber, pUser.address,
        pUser.dob,
        UserRole.BORROWER, pUser.nationality.toInt, false,
        "", null, "", null, None)
    } else {
      OBUser(pUser.seqNo, pUser.userID, pUser.name,
        Gender(pUser.gender),
        pUser.idNumber, pUser.address,
        pUser.dob,
        UserRole(pUser.userRoleID.toInt), pUser.nationality.toInt, false,
        "", null, "", null, None)
    }
  }

  def find(pSeqNo: Int): TUser = {
   commonService.findUserBySeqNo(pSeqNo) 
  }

  def findByUserID(pUserID: String): TUser = {
    commonService.findByUserID(pUserID: String)
  }
  
  def findByIDNumber(pIDNumber: String): TUser = {
   commonService.findUserByIDNumber(pIDNumber)
  }
  
  val sdf = new SimpleDateFormat("dd-MM-yyyy")
}