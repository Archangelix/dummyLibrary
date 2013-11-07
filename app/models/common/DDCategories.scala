package models.common

import services.DBService

case class DDCategories(code: String, desc: String)

object DDCategories {
  def apply(pCode: String): DDCategories = {
    DDCategories(pCode, Category.all(pCode))
  }
  
  def all = Category.all.toList.sortBy(_._2)
}