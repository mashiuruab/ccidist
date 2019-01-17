CCIDIST
=======

This is the code repository of the CCI Distribution Server. The server provides access to
epubs (http://idpf.org/epub/30) trough a set of RESTful web services.

Installation prerequisites
--------------------------

The distribution server is written in Java 7. The build system it Maven 3. Thus, to be able to clone, build and run
this application you need the following to be installed on your computer

 1. Git.

    Git must be configured with the necessary certificates to connect to your GitHub account.

 2. Java 7

 3. Maven 3

 4. A MySQL database.

    According to plan, the distribution service will eventually support Oracle, but currently it
    has only been tested with MySQL

Installation instructions
-------------------------

1. Change directory to wherever you store your git projects

   e.g. `cd ~/gitrepos`

   You do  ot need to create the root directory of the as this will be create when the project is cloned in the next
   step.

2. Clone the CCIDIST repository

   `git clone https://github.com/Cefalo/ccidist.git`

3. Change directory to the project root

   `cd ccidist`

4. Build the application

   `mvn clean package`

   This will download all necessary packages, compile, and package the server

5. Create the database tables and populate them with some sample data.

   This is the most shaky part of the installation, because you need to edit an sql script.
   There is a script that will create the database, create it's tables, and insert some sample data into it. The script
   can be found here:

   `ccidist/ccidist-model/src/main/resources/migration.sql`

   The section "epub files" in the script inserts some sample epub files into the database. The script needs to be
   edited to provide the absolute path of these files. Do this by replacing the string `<absolute-root> with the
   corresponding path on you machine.

6. Make sure the application can access the database

   By default the application tries to connect to the database server with username 'root' and no password.
   If your database server will not accept this, you can set the correct user name and password by editing the file

   `ccidist/ccidist-ws/src/main/resources/META-INF/persistence.xml`

7. Start the server

    `cd ccidist-ws`
    `mvn jetty:run`

8. Test that the server works

   Point your browser at `http://localhost:8080/cciService` to get the root document, and navigate from there.

How to prepare a release
------------------------
* Make sure that there is no uncommitted change in the source tree.

* Make sure that you have updated the documentation and release notes.

* Make sure that normal maven build works on your system. Also make sure that
git works properly on your setup.

* Maven uses the "git" command. Do a `git pull` to warm up credential cache or
git related stuff. Without this, things seem to fail.

* **It is strongly recommended to use git-credential-cache so that git can
remember your user/pass.**

* Run `mvn -Dusername=your_github_username -Dpassword=your_github_password release:prepare release:perform`

* Your release build of the distribition is at ./target/checkout/ccidist-dist/target/.

* The maven command tags the source tree for the release. To re-create the release,
`git checkout tagged_released_version` and build with `mvn clean install`.
After the build finishes, the release distribution will be under ./ccidist-dist/target.
