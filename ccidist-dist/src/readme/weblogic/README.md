Weblogic Installation Procedure
================================

* Install latest version of Weblogic. For example, I used wls1212.zip and my 
Installation directory is /usr/local/weblogic.

* Add following code to config.xml. It is in  /usr/local/weblogic/user_projects/domains/mydomain/config/  
    **Note:** add this two line at the end of <security-configuration> tag.
    ```
    <enforce-strict-url-pattern>false</enforce-strict-url-pattern>
    <enforce-valid-basic-auth-credentials>false</enforce-valid-basic-auth-credentials>
    ```
* Run weblogic with ./usr/local/weblogic/user_projects/domains/mydomain/bin/startWeblogic.sh.

* Click deployments (top-left corner).

* Then click install.

* There is  upload your file(s) link and click.

* Choose webservice/admin war from ccidist-ws/ccidist-admin  target.

* Click next , next, next and finally finish.

* Access webservice app by http://localhost:7001/webservice.  
    **NOTE:** To increase Weblogic's heap size. You can add following 
    configuration parameters in setDomainEnv.sh (/usr/local/weblogic/user_projects/domains/mydomain/bin)
    after WL_HOME line:
    ```
    USER_MEM_ARGS="-Xms1024m -Xmx1536m -XX:MaxPermSize=768m"
    export USER_MEM_ARGS
    ```
