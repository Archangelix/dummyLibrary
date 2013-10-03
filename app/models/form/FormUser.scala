package models.form

import java.util.Date
import models.OBUser
import java.text.SimpleDateFormat

/**
 * User form object. A business object has to be mapped to / from this form object 
 * in order to communicate with the Form.
 * 
 * FormUser doesn't have the <code>isDeleted</code> attribute. There should never be a situation where
 * a deleted user comes from / to the View layer. 
 * In this layer by default <code>isDeleted</code> attribute is always <code>false</code>.
 */
case class FormUser(
    rowIdx: Option[Long],
    seqNo: Option[Long], 
    userID: String, 
    name: String, 
    gender: String, 
    idNumber: String, 
    address: String,
    dob_date: String,
    dob_month: String,
    dob_year: String,
    userRoleID: String,
    userRoleName: Option[String],
    nationality: String,
    password: String
)

object FormUser {
  def apply(pUser: OBUser): FormUser = {
    FormUser(pUser.rowIdx, pUser.seqNo, pUser.userID, pUser.name, pUser.gender.toString(), 
        pUser.idNumber, pUser.address,
        sdf_date.format(pUser.dob), sdf_month.format(pUser.dob), sdf_year.format(pUser.dob),
        pUser.role.id.toString, Some(pUser.role.name), pUser.nationality.toString, "")
  }
  
  val sdf_date = new SimpleDateFormat("d")
  val sdf_month = new SimpleDateFormat("M")
  val sdf_year = new SimpleDateFormat("yyyy")
  
}

