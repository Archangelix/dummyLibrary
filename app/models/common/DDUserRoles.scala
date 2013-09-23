package models.common

import services.DBService
import models.OBUserRole

case class DDUserRoles(code: String, desc: String)

object DDUserRoles {
  def apply(pCode: String): DDUserRoles = {
    DDUserRoles(pCode, OBUserRole.all(pCode))
  }
  
  def all = OBUserRole.all.toList.sortBy(_._2)
}