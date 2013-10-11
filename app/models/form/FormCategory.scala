package models.form

import models.OBCategory

case class FormCategory(selectedID: Option[Long], updatedCategoryName: String, 
    newCategoryName: String, list: Option[List[OBCategory]])
