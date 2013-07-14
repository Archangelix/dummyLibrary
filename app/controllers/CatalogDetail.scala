package controllers

import java.text.SimpleDateFormat
import java.util.Calendar
import models.Catalog
import models.db.DBCatalog
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc.Action
import play.api.mvc.Controller
import services.DBService

object CatalogDetail extends Controller {

  val MODE_ADD = "ADD"
  val MODE_EDIT = "EDIT"
    
  val catalogForm = Form[Catalog](
    mapping(
      "idx" -> optional(of[Long]),
      "id" -> optional(of[Long]),
      "title" -> nonEmptyText,
      "author" -> nonEmptyText,
      "publishedYear" -> {
        val currentTime = Calendar.getInstance().getTime()
        val yearFormat = new SimpleDateFormat("yyyy")
        val currentYear = yearFormat.format(currentTime).toInt
        number(min = 0, max = currentYear)
      })(Catalog.apply)(Catalog.unapply)
    verifying("Duplicate catalog found.", {
      catalog => catalog.id==null || {
        val dbCatalogs = DBService.findDuplicates(catalog)
        dbCatalogs.size==0 || dbCatalogs.size==1 && dbCatalogs.get(0).id==catalog.id
      }
    })
  )

  def gotoNewCatalog() = Action { implicit req =>
    Ok(views.html.newCatalog(MODE_ADD, catalogForm)(session)).withSession(
        session + ("mode" -> MODE_ADD))
  }

  def edit(pIDStr: String) = Action { implicit req =>
    val catalog = DBService.findCatalogByID(pIDStr.toInt, true)
    val filledForm = catalogForm.fill(catalog)
    Ok(views.html.newCatalog(MODE_EDIT, filledForm)(session)).withSession(
        session + ("mode" -> MODE_EDIT))
  }
  
  def save = Action { implicit req =>
    val tempForm = catalogForm.bindFromRequest()
    val mode = session.get("mode").getOrElse(MODE_ADD)

    tempForm.fold(
      errors => {
        println("Mode = "+mode)
        tempForm.errors.map {err => 
          println(err.message)
        }
        BadRequest(views.html.newCatalog(mode, tempForm)(session))
      },
      data => {
        if (mode.equals(MODE_ADD)) {
          DBService.createCatalog(data.title, data.author, data.publishedYear)
        } else {
          DBService.updateCatalog(data.id.get, data.title, data.author, data.publishedYear)
        }
        Redirect(routes.Application.index()).withSession(session - "mode")
      })
  }

}