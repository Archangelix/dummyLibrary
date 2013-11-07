package models.form

import models.common.Category

case class FormCategory(
    selectedID: Option[Int], 
    updatedCategoryName: Option[String], 
    newCategoryName: Option[String], 
    list: Option[List[Category]])
