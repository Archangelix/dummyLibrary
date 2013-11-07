package models.common

import services.DBService

case class Category(code: Int, name: String) {
  override def toString() = name
}

object Category {
  private def items = DBService.getCategoriesMap

  def apply(pID: Int): Category = Category(pID, items(pID.toString))
  
  def all = items
  
  def alls = DBService.listAllCategories()
  
  def findByName(pCategoryName: String) = DBService.findCategoryByName(pCategoryName)
  
}