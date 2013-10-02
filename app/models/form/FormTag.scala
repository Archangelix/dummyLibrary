package models.form

import models.OBTag

case class FormTag(selectedID: Option[Long], updatedTagName: String, 
    newTagName: String, list: Option[List[OBTag]])
