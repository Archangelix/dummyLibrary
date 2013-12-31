package models.form

import models.TCatalog

/**
 * Book form object. A business object has to be mapped to / from this form object 
 * in order to communicate with the Form.
 * 
 * FormBook doesn't have the <code>isDeleted</code> attribute. There should never be a situation where
 * a deleted book comes from / to the View layer. 
 * In this layer by default <code>isDeleted</code> attribute is always <code>false</code>.
 */
case class FormSearchCatalog(
    searchKeyword: String, 
    author: String, 
    title: String, 
    caseSensitive: String)

object FormSearchCatalog {
  def apply(pCatalog: TCatalog, pCaseSensitive: Boolean): FormSearchCatalog = {
    FormSearchCatalog("", pCatalog.author, pCatalog.title, if (pCaseSensitive) "Y" else "N")
  }
}