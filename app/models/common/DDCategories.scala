package models.common

import services.DBService
import models.OBCategory

case class DDCategories(code: String, desc: String)

object DDCategories {
  def apply(pCode: String): DDCategories = {
    DDCategories(pCode, OBCategory.all(pCode))
  }
  
  def all = OBCategory.all.toList.sortBy(_._2)
}