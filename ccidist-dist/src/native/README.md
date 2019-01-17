Jetty SetUID plugin for CCI Distribution Service
===================================================

CCIDist uses a native library to run the application server on privileged 
ports, for example: port 80. So the webserver starts up as root user, grabs 
port 80 and then starts the actual web application as the "ccidist" user.

Since this native library code runs with **super-user permission**, security is
of utmost importance. The bundled libraries with CCIDist were compiled from 
source and are guaranteed to be safe. However, if you prefer, you can also 
build them yourself.

To build this library, set JAVA_HOME environment variable to point to a JDK
(**NOTE: A JRE won't work**) and issue "make" in this directory. This will create
libsetuid.so in this folder. Depending on the architecture of the build
machine, copy libsetuid.so to either x86 or x86_64 directory.

Thanks :-)
