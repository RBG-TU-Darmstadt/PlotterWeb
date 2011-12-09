# PlotterWeb

A small webinterface allowing PDF printing on plotters developed during a university project. Uses the TU Darmstadt Single Sign-On for authentication.

## Developer Guide
1. Clone this project
2. Install the Eclipse IDE for Java EE Developers
    * Install Maven for Eclipse: [m2e](http://download.eclipse.org/technology/m2e/milestones/1.0), [m2e-wtp](http://download.jboss.org/jbosstools/updates/m2eclipse-wtp)
    * Install tomcat, Windows -> Preferences -> Servers -> Runtime Environments -> Add 
4. Import sourcecode:
    * File -> Import -> Maven -> Maven Projects, choose the folder from step 1, in Advanced select Naming template: `[groupId].[artifactId]`
5. Install PostgreSQL (see below)
6. Install [GhostScript](http://www.ghostscript.com/)
7. Configure project (see below)

## PostgreSQL Setup
1. Install PostgreSQL Server
2. Create database
    * Database name: `PlotterWeb`
3. Edit `hibernate.cfg.xml` located in `src/src/main/resources`, depending on your configuration set properties
    * connection.url
    * connection.username
    * connection.password

## Configure project
1. Edit `configuration.properties` located in `src/src/main/webapp/WEB-INF` and set 
    * Prices
    * Path to GhostScripts `convert` binary
    * Allowed billing users
    * Plotter name and margin

## Build and install the project

The entire project can be build with `mvn package`. This creates a .war-files in the target-directories of the project.

To install the applications, move the .war-files to the Tomcat webapps directory.

### Build from Eclipse
1. Right click on the corresponding project
2. Choose `Run-As` -> `Maven install`

This creates a .war-files in the target-directories of the project.

# Authors

Lennart Diedrich, diedrich@rbg.informatik.tu-darmstadt.de  
Samuel Vogel, vogel@rbg.informatik.tu-darmstadt.de
