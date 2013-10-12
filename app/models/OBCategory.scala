package models

import services.DBService

case class OBCategory(code: Int, name: String) {
  override def toString() = name
}

object OBCategory {
  private lazy val items = DBService.getCategoriesMap

  def apply(pID: Int): OBCategory = OBCategory(pID, items(pID.toString))
  
  def all = items
  
}