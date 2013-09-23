package models

import services.DBService

case class OBUserRole(id: Long, name: String)

object OBUserRole {
  private lazy val items = DBService.getUserRolesMap

  def apply(pID: Long): OBUserRole = OBUserRole(pID, items(pID.toString))
  
  def all = items
}