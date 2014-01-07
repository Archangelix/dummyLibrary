package controllers

import org.junit.Test
import play.api.libs.json.Json
import play.api.libs.json.JsString
import play.api.test.FakeRequest
import play.api.mvc.Controller
import play.api.mvc.SimpleResult
import play.api.test.PlaySpecification
import play.api.test.Helpers._
import services.TDBService
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito.when
import services.PSQLService
import models.TCUser
import models.OBUser
import models.common.UserRole
import play.mvc.Results.Redirect
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import play.api.test.WithApplication
import play.api.test.FakeApplication

class ABLoginSignupTest extends FlatSpec with Matchers with MockitoSugar {

  val SALTED_ADMIN_PASSWORD = "[B@249a39df|fae50dfd4903e8e2faf931fc11784430f46418e2fd7f39d21ab6615b12262f19"
    
  class TestController extends Controller with ABLoginSignup {
    override val dbService = mock[TDBService]
	when(dbService.getPassword("admin")).thenReturn(SALTED_ADMIN_PASSWORD)
	
	override val objUser = mock[TCUser]
    when(objUser.findByUserID("admin")).thenReturn(OBUser(
        seqNo=None, 
        userID="", 
        name="", 
        gender=null, 
        idNumber="", 
        address="", 
        dob=null, 
        role=UserRole.ADMIN, 
        nationality=0,
        isDeleted=false,
        createUsercode="",
        createTimestamp=null,
        auditUsercode="",
        auditTimestamp=null,
        auditReason=null
    ))
  }
  
  "The guest" should "have access to loginsignup page" in {
    val objABLogin = new TestController
    val res = objABLogin.loginPage().apply(FakeRequest())
    res should not be null
    contentType(res) should be(Some("text/html"))
    contentAsString(res) should include("Login Information")
  }
  
/*  it should "be able to login from the loginsignup page" in new WithApplication(
      new FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
    val objABLogin = new TestController
    val json = Json.obj(
      "username" -> "admin",
      "password" -> "admin")
    val req = FakeRequest("POST", "/loginsignup").withJsonBody(json)
    val res = objABLogin.login().apply(req)
    res should not be null
    status(res) should be(SEE_OTHER)
    redirectLocation(res) should be(Some("/useradmin"))
  }
  
  it should "fail the login with the wrong username/password in the main page" in new WithApplication(
      new FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
    val objABLogin = new TestController
    val json = Json.obj(
      "username" -> "admin",
      "password" -> "admins")
    val req = FakeRequest("POST", "/login").withJsonBody(json)
    val res = objABLogin.login().apply(req)
    res should not be null
    status(res) should be(BAD_REQUEST)
    redirectLocation(res) should be(Some("/loginsignup"))
  }
  
  it should "have access to search result page" in new WithApplication(
      new FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
    
  }*/
}