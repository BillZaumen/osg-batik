
#
# https://stackoverflow.com/questions/16138850/
# batik-svggraphics2d-nullpointerexception-when-drawing-image
# indicates that batik-codec.jar has to be on the class path.
# batik-all.jar curiously does not include this. Without batik-codec,
# you will get a null pointer exception but no indication that
# a class was missing.

BATIKPATH = $(shell echo -n ; \
	for i in batik-svggen batik-dom batik-svg-dom batik-constants \
		batik-util batik-ext batik-awt-util \
		batik-i18n batik-xml batik-codec \
	; do echo -n :/usr/share/java/$$i.jar ; done)


BATIK = ../BUILD/batik-osgmod.jar
PROVIDER = ../BUILD/libosgbatik.jar


# BZDEV = /usr/share/java/libbzdev.jar
BZDEVLIBS1 = /usr/share/java/libbzdev-base.jar
BZDEVLIBS2 = /usr/share/java/libbzdev-obnaming.jar
BZDEVLIBS3 = /usr/share/java/libbzdev-desktop.jar
BZDEVLIBS = $(BZDEVLIBS1):$(BZDEVLIBS2):$(BZDEVLIBS3)
SECPOLICY = /usr/share/bzdev/libbzdev.policy


# temporarily remove the provider

JAVA = java -Djava.security.policy=test.policy \
	-classpath classes:$(BZDEVLIBS):$(BATIK):$(PROVIDER)

JAVAM = java -Djava.security.policy=test.policy \
	-p $(BZDEVLIBS):$(BATIK):$(PROVIDER) \
	--add-modules org.bzdev.desktop \
	-classpath classes


all: test.policy
	(cd .. ; make )
	mkdir -p classes
	javac -d classes -classpath $(BZDEVLIBS) Test.java
	rm -f *.svg
	@echo -------Test 1 ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVA) Test svg  \
		|| echo ... skipping
	@echo ------- Test 2 ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVA) Test svg NORMAL \
		|| echo ... skipping
	@echo ------- Test 1cw ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVA) Test svg	\
		CLOCKWISE90 || echo ... skipping
	@echo ------- Test 1ccw ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVA) Test svg \
		COUNTERCLOCKWISE90 || echo ... skipping
	@echo ------- Test 3 ----------------
	@$(JAVA) Test svgz


test.policy:
	cat /usr/share/bzdev/libbzdev.policy > test.policy

modtest: test.policy
	(cd .. ; make )
	mkdir -p classes
	javac -d classes -classpath $(BZDEVLIBS) Test.java
	rm -f *.svg
	@echo -------Test 1 ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVAM) Test svg  \
		|| echo ... skipping
	@echo ------- Test 2 ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVAM) Test svg NORMAL \
		|| echo ... skipping
	@echo ------- Test 1cw ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVAM) Test svg	\
		CLOCKWISE90 || echo ... skipping
	@echo ------- Test 1ccw ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVAM) Test svg \
		COUNTERCLOCKWISE90 || echo ... skipping
	@echo ------- Test 3 ----------------
	@$(JAVAM) Test svgz





# Test using Batik directly instead of via a service provider.
# This is basically a sanity check of the Batik installation
# and a test that we've found the correct class path.

batik: classes/BatikTest.class
	java -classpath classes:$(BATIK) BatikTest batik.svg

classes:
	mkdir -p classes

# Verify if the libosgbatik jar file specifies the Batik jar files
# in its manifest.

batik2: classes/BatikTest.class
	java -classpath classes:../BUILD/libosgbatik.jar BatikTest batik2.svg


classes/BatikTest.class: classes BatikTest.java
	javac -d classes -classpath $(BATIK) BatikTest.java

tstjar.jar:
	mkdir -p classes1 
	rm -f classes1/module-info.class
	(cd tstmodule ; javac -d ../classes1 `find org -name '*.java'` )
	jar cf tstjar.jar -C classes1 .

btest1: tstjar.jar
	(cd .. ; make )
	mkdir -p classes
	javac -d classes -classpath $(BZDEV):classes Test.java
	java -classpath classes:tstjar.jar:../BUILD/libosgbatik.jar \
		Test svg NORMAL

clean:
	rm -f test.policy
	rm -rf classes
	rm -f *.svg
