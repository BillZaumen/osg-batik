
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
BZDEVLIBS3 = /usr/share/java/libbzdev-math.jar
BZDEVLIBS4 = /usr/share/java/libbzdev-graphics.jar
BZDEVLIBS = $(BZDEVLIBS1):$(BZDEVLIBS2):$(BZDEVLIBS3):$(BZDEVLIBS4)
SECPOLICY = /usr/share/bzdev/libbzdev.policy


# temporarily remove the provider

JAVA = java -Djava.security.policy=test.policy \
	-classpath classes:$(BZDEVLIBS):$(PROVIDER)

JAVAM = java -Djava.security.policy=test.policy \
	-p $(BZDEVLIBS):$(PROVIDER) \
	--add-modules org.bzdev.graphics \
	-classpath classes


all: test.policy batik
	@echo NOW TEST OSG PROVIDER
	(cd .. ; make )
	mkdir -p classes
	javac -d classes -classpath $(BZDEVLIBS) Test.java

	@echo CLASSPATH TESTS ...
	rm -f *.svg
	@echo -------Test 1 ----------------
	$(JAVA) Test svg
	@echo ------- Test 2 ----------------
	 $(JAVA) Test svg NORMAL
	@echo ------- Test 1cw ----------------
	$(JAVA) Test svg CLOCKWISE90
	@echo ------- Test 1ccw ----------------
	$(JAVA) Test svg COUNTERCLOCKWISE90
	@echo ------- Test 3 ----------------
	@$(JAVA) Test svgz
	@echo MODULE-OPTIONS TESTS ...
	rm -f *.svg
	@echo -------Test 1 ----------------
	$(JAVAM) Test svg
	@echo ------- Test 2 ----------------
	$(JAVAM) Test svg NORMAL
	@echo ------- Test 1cw ----------------
	$(JAVAM) Test svg CLOCKWISE90
	@echo ------- Test 1ccw ----------------
	$(JAVAM) Test svg COUNTERCLOCKWISE90
	@echo ------- Test 3 ----------------
	@$(JAVAM) Test svgz

test.policy:
	cat /usr/share/bzdev/libbzdev.policy > test.policy


# Test using Batik directly instead of via a service provider.
# This is basically a sanity check of the Batik installation
# and a test that libosgbatik jar file contains enough of Batik
# to function.

batik: classes classes/BatikTest.class
	@echo TEST THAT libosgbatik.jar CONTAINS ENOUGH OF BATIK
	(cd .. ; make )
	java -classpath classes:../BUILD/libosgbatik.jar BatikTest batik.svg

classes:
	mkdir -p classes

classes/BatikTest.class: classes BatikTest.java
	(cd .. ; make )
	javac -d classes -classpath ../BUILD/libosgbatik.jar BatikTest.java

clean:
	rm -f test.policy
	rm -rf classes
	rm -f *.svg
	rm -f *.svgz
