package models.form

import models.OBCategory

case class FormCategory(selectedID: Option[Int], updatedCategoryName: String, 
    newCategoryName: String, list: Option[List[OBCategory]])
