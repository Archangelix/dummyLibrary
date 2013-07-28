INTRODUCTION
============

Dummy Library is a simple web application meant to exercise the Scala & Play framework web application in more practical way.

FUNCTIONAL SPECIFICATION
========================

Dummy Library consists of two categories of functionalities, each will be explained in more details:

 - Admin functionalities
 - Borrower functionalities (TODO)

Admin Functionalities
-------------------
As an admin, the user will have the rights to organise the web application in general. This includes:

 - User management (Adding the users / borrowers, granting the roles, resetting passwords, etc.)
 - Catalog management (Adding new catalogs / editing, archive, etc.)

Borrower Functionalities
------------------------
As a borrower, the user will be granted these functionalities:

 - Making a booking for an existing catalog (The catalog exists in the library) so that he / she will be informed once it is available to borrow.
 - Informing the library regarding the books that he / she wants to borrow but not yet available in the library, i.e. ***Wishlist***.
 - Monitor the books that the user has borrowed and also returned.
 - Monitor the books that have been borrowed and but not yet returned.
 - Monitor the books that he / she wants to borrow.


TECHNICAL SPECIFICATION
=======================
This web application is built on Play 2.x framework by using Scala 2.10 and deployed to Heroku server. The web application itself adopts the MVC (Model-View-Controller) model.

There are three layers. Most of the entities present in this application are represented in three different forms, one for each layer.
 - UI (User Interface) layer. 
 - Business layer.
 - Database layer.

UI Layer
--------
This layer is responsible *only* to display the entities and / or capture the information inputted by the user on the screen. There are no business and database activities applied on the objects present in this layer.

All the class names respresenting the entities will be prefixed with word 'Form', for example: FormUserPassword, FormBook, FormCatalog, etc.

Business Layer
--------------
This layer is responsible *only* to do business processes. There are no UI and database activities applied in this layer.

All the class names will be prefixed with either 'OB' (for classes / objects), 'T' (for Traits), for example: OBUserPassword, TSecured, etc.

Database Layer
--------------
This layer is responsible *only* to do database processes. There are no UI and business activities applied in this layer. Basically, there are only four *legal* activities in this layer: Insert, Update, Delete, and Query.

Currently this layer is still tied together with the Bridge, and to be implemented in the future.

*Bridges*
-------
The term *bridge* here is used to connect two different layers. There are two bridges:

 - **Controller.** The bridge between **UI Layer** and **Business Layer** takes place.
All the class names respresenting the controller will be prefixed with 'AB', for example: ABLogin, ABCatalogDetail, ABCatalogList, etc.

 - **Service.** The bridge between **Business Layer** and **Database Layer** takes place.
All the class names will be prefixed with either 'DB', for example: DBService.

