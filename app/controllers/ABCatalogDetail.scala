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
import controllers.util.MySession

/**
 * Action to handle the catalog section, including the add, update, delete, and view.
 */
object ABCatalogDetail extends Controller with TSecured {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
  
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
      }
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
    Ok(views.html.catalog_detail(MODE_ADD, catalogForm, List())(session)).withSession(
        session + ("mode" -> MODE_ADD))
  }

  /**
   * Displaying the book detail page with pre-populated catalog information.
   */
  def edit(pIDStr: String) = withAuth {username => implicit req =>
    val catalog = DBService.findCatalogByID(pIDStr.toInt, true)
    val formCatalog = FormCatalog(catalog)
    val filledForm = catalogForm.fill(formCatalog)
    val formBooks = catalog.books.getOrElse(List()).map {
      book => FormBook(book)
    }
    
    val username = session.get("username").getOrElse("")
    MySession.put(username, "formBooks", formBooks)
    Ok(views.html.catalog_detail(MODE_EDIT, filledForm, formBooks)(session)).withSession(
        session + ("mode" -> MODE_EDIT))
  }
  
  /**
   * Saving the catalog details.
   */
  def save = withAuth {username => implicit req =>
    val tempForm = catalogForm.bindFromRequest()
    val mode = session.get("mode").getOrElse(MODE_ADD)
    val username = session.get("username").getOrElse("")
    val flashBooks = MySession.get(username, "formBooks")

    tempForm.fold(
      errors => {
        tempForm.errors.map {err => 
          println(err.message)
        }
        val formBooks = flashBooks.asInstanceOf[List[FormBook]]
        BadRequest(views.html.catalog_detail(mode, tempForm, formBooks)(session))
      },
      data => {
        if (mode.equals(MODE_ADD)) {
          DBService.createCatalog(data.title, data.author, data.publishedYear)
        } else {
          DBService.updateCatalog(data.id.get, data.title, data.author, data.publishedYear)
        }
        Redirect(routes.ABCatalogList.index()).withSession(session - "mode")
      })
  }

}