package controllers

import java.text.SimpleDateFormat
import java.util.Calendar
import models.OBCatalog
import models.db.DBCatalog
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc.Action
import play.api.mvc.Controller
import services.DBService
import play.api.data.Forms
import models.form.FormBook
import models.form.FormCatalog
import play.api.data.Field

/**
 * Action to handle the catalog section, including the add, update, delete, and view.
 */
object ABCatalogDetail extends Controller with TSecured {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
  
  val formBookMapping = mapping(
    "idx" -> optional(of[Long]),
    "id" -> optional(of[Long]),
    "catalogID" -> of[Long],
    "originCode" -> nonEmptyText,
    "originDesc" -> optional(text),
    "remarks" -> nonEmptyText
  )(FormBook.apply)(FormBook.unapply)

    
  val formCatalogMapping = mapping(
      "idx" -> optional(of[Long]),
      "id" -> optional(of[Long]),
      "title" -> nonEmptyText,
      "author" -> nonEmptyText,
      "publishedYear" -> {
        val currentTime = Calendar.getInstance().getTime()
        val yearFormat = new SimpleDateFormat("yyyy")
        val currentYear = yearFormat.format(currentTime).toInt
        number(min = 0, max = currentYear)
      },
      "category" -> number,
      "books" -> optional(list(formBookMapping))
    )(FormCatalog.apply)(FormCatalog.unapply)

  val catalogForm = Form[FormCatalog](
    formCatalogMapping verifying ("Duplicate catalog found.", {
      formCatalog =>
        formCatalog.id == null || {
          // There shouldn't be any duplicate catalogs in the database.
          val dbCatalogs = DBService.findDuplicates(OBCatalog(formCatalog, List()))
          dbCatalogs.size == 0 || dbCatalogs.size == 1 && dbCatalogs.get(0).id == formCatalog.id
        }
    }))

  /**
   * Displaying the catalog detail page with blank information.
   */
  def gotoNewCatalog() = withAuth {username => implicit req =>
    Ok(views.html.catalog_detail(MODE_ADD, catalogForm)(session)).withSession(
        session + ("mode" -> MODE_ADD))
  }

  /**
   * Displaying the book detail page with pre-populated catalog information.
   */
  def edit(pIDStr: String) = withAuth {username => implicit req =>
    val catalog = DBService.findCatalogByID(pIDStr.toInt, true)
    val formCatalog = FormCatalog(catalog)
    val filledForm = catalogForm.fill(formCatalog)
    
    val username = session.get("username").getOrElse("")
    Ok(views.html.catalog_detail(MODE_EDIT, filledForm)(session)).withSession(
        session + ("mode" -> MODE_EDIT))
  }
  
  /**
   * Saving the catalog details.
   */
  def save = withAuth {username => implicit req =>
    println("save!!!")
    val tempForm = catalogForm.bindFromRequest()
    val mode = session.get("mode").getOrElse(MODE_ADD)
    val username = session.get("username").getOrElse("")

    tempForm.fold(
      errors => {
        tempForm.errors.map {err => 
          println(err.message)
        }
        BadRequest(views.html.catalog_detail(mode, tempForm)(session))
      },
      data => {
        if (mode.equals(MODE_ADD)) {
          DBService.createCatalog(data.title, data.author, data.publishedYear, data.category)
        } else {
          DBService.updateCatalog(
              data.id.get, data.title, data.author, data.publishedYear, data.category)
        }
        Redirect(routes.ABCatalogList.index()).withSession(session - "mode")
      })
  }

}