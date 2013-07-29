package models.form

import models.OBUser
import java.util.Date

/**
 * User form object. A business object has to be mapped to / from this form object 
 * in order to communicate with the Form.
 * 
 * FormUser doesn't have the <code>isDeleted</code> attribute. There should never be a situation where
 * a deleted user comes from / to the View layer. 
 * In this layer by default <code>isDeleted</code> attribute is always <code>false</code>.
 */
case class FormUser(
    rowIdx: Long,
    seqNo: Option[Long], 
    userID: String, 
    name: String, 
    address: String,
    dob: Date,
    userRoleID: Long,
    userRoleName: String
)

object FormUser {
  def apply(pUser: OBUser): FormUser = {
    FormUser(pUser.rowIdx, pUser.seqNo, pUser.userID, pUser.name, pUser.address, 
        pUser.dob, pUser.role.id, pUser.role.name)
  }
}

