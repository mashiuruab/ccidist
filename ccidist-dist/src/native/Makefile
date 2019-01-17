LIB_NAME=libsetuid.so

build:
	gcc -I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/linux/ -fPIC -shared org_mortbay_setuid_SetUID.c -o $(LIB_NAME)

clean:
	rm -f $(LIB_NAME)
