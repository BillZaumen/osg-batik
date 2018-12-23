#
# GNU Make file.
#
#
# Set this if  'make install' should install its files into a
# user directory - useful for package systems that will grab
# all the files they see.  Setting this will allow a package
# to be built without requiring root permissions.
#
DESTDIR :=

JROOT := $(shell while [ ! -d src -a `pwd` != / ] ; do cd .. ; done ; pwd)

include VersionVars.mk

CLASSES = $(JROOT)/classes
TMPSRC = $(JROOT)/tmpsrc
#
# System directories (that contains JAR files, etc.)
#
SYS_LIBJARDIR = /usr/share/java
SYS_API_DOCDIR = /usr/share/doc/bzdev-osgbatik-doc

#
# https://stackoverflow.com/questions/16138850/
# batik-svggraphics2d-nullpointerexception-when-drawing-image
# indicates that batik-codec.jar has to be on the class path.
# batik-all.jar curiously does not include this. Without batik-codec,
# you will get a null pointer exception but no indication that
# a class was missing.

BATIK_NAMES = batik-svggen batik-dom batik-svg-dom batik-constants \
	batik-util batik-ext batik-awt-util \
	batik-i18n batik-xml batik-codec

BATIK_JARS = $(shell for i in $(BATIK_NAMES); \
		do echo $(SYS_LIBJARDIR)/$$i.jar ; done)


#BATIK_JARS = $(shell for i in $(BATIK_NAMES); \
#		do echo BUILD/$$i.jar ; done)

#BATIKPATH = $(shell echo -n ; \
#	for i in $(BATIK_NAMES) ; \
#		do echo -n :BUILD/$$i.jar ; done)
BATIKPATH = BUILD/batik-osgmod.jar

BZDEV_MODULES = base desktop
BZDEV_JARS = $(shell for i in $(BZDEV_MODULES); \
		do echo BUILD/libbzdev-$$i.jar ; done)


EXTLIBS1=BUILD/libbzdev-base.jar:BUILD/libbzdev-desktop.jar
EXTLIBS=$(EXTLIBS1):$(BATIKPATH)

ALL = jarfile

all: $(ALL)

# Target for the standard Java extension directory
LIBJARDIR = $(DESTDIR)$(SYS_LIBJARDIR)

LIBJARDIR_SED=$(shell echo $(SYS_LIBJARDIR) | sed  s/\\//\\\\\\\\\\//g)

# Other target directories

API_DOCDIR = $(DESTDIR)$(SYS_API_DOCDIR)

JROOT_JARDIR = $(JROOT)/BUILD
JROOT_LIBJARDIR = $(JROOT_JARDIR)

JDOCS = *.html stylesheet.css package-list
RDOCS = *.gif


BZDEV_JARS = $(shell for i in $(BZDEV_MODULES); \
		do echo BUILD/libbzdev-$$i.jar ; done)

all: $(JARFILE)

test: BUILD/batik-osgmod.jar

# We don't seem to need the service providers that Batik supports.
BUILD/batik-osgmod.jar:
	mkdir -p BUILD
	rm -fr BUILD/osgmod
	mkdir BUILD/osgmod
	for i in $(BATIK_JARS) ; \
		do unzip -o -d BUILD/osgmod $$i > /dev/null; done
	rm -rf BUILD/osgmod/org/w3c
	rm -rf BUILD/osgmod/META-INF/maven
	rm -rf BUILD/osgmod/META-INF/MANIFEST.MF
	jar --create --file BUILD/batik-osgmod.jar \
	     -C BUILD/osgmod .
	jar --create --file BUILD/batik-osgmod.jar \
		-m manifest.osgmod -C BUILD/osgmod .
	rm -rf BUILD/osgmod


$(BZDEV_JARS):
	mkdir -p BUILD
	ln -s $(subst BUILD,$(SYS_LIBJARDIR),$@) $@



OSGBATIK_DIR = ./src/org.bzdev.osgbatik
BZDEV = org/bzdev


MANIFEST_CLASS_PATH = $(shell echo -n Class-Path: ; \
	for i in $(BATIK_NAMES)	; do echo -n " "./$$i.jar ; done )


OSGBATIK_JFILES = $(wildcard $(OSGBATIK_DIR)/$(BZDEV)/providers/osgbatik/*.java)

LPACK = providers/osgbatik/lpack
OSGP = $(wildcard $(OSGBATIK_DIR)/$(BZDEV)/$(LPACK)/*.properties)
OSGBATIK_RESOURCES1 = $(OSGP)
OSGBATIK_RESOURCES =$(subst $(OSGBATIK_DIR)/,,$(OSGBATIK_RESOURCES1))

include MajorMinor.mk

JARFILE = $(JROOT_LIBJARDIR)/libosgbatik.jar

BATIKJAR = $(shell [ -f /usr/share/java/batik.jar ] && \
	echo ":/usr/share/java/batik.jar"  || \
	echo  "" )

OSGBATIK_MODS = mods/org.bzdev.osgbatik


jarfile: $(JARFILE)

$(JARFILE): $(OSGBATIK_JFILES) $(BZDEV_JARS) $(BATIKPATH) \
	$(OSGBATIK_DIR)/module-info.java \
	$(OSGBATIK_DIR)/META-INF/services/org.bzdev.gio.spi.OSGProvider
	mkdir -p mods/org.bzdev.osgbatik
	mkdir -p BUILD
	javac -d mods/org.bzdev.osgbatik -p $(EXTLIBS) \
		$(OSGBATIK_DIR)/module-info.java $(OSGBATIK_JFILES) \
	  $(OSGBATIK_DIR)/org/bzdev/providers/osgbatik/lpack/DefaultClass.java
	mkdir -p mods/org.bzdev.osgbatik/META-INF/services
	for i in $(OSGBATIK_RESOURCES) ; \
		do mkdir -p $(OSGBATIK_MODS)/`dirname $$i` ; \
                   cp $(OSGBATIK_DIR)/$$i $(OSGBATIK_MODS)/$$i ;\
                   echo cp $(OSGBATIK_DIR)/$$i $(OSGBATIK_MODS)/$$i ;\
		done
	cp $(OSGBATIK_DIR)/META-INF/services/org.bzdev.gio.spi.OSGProvider \
	   $(OSGBATIK_MODS)/META-INF/services/org.bzdev.gio.spi.OSGProvider
	jar --create --file $(JARFILE) --manifest $(OSGBATIK_DIR)/manifest.mf \
		copyright -C mods/org.bzdev.osgbatik .

clean:
	@[ -d $(CLASSES) ] && rm -rf $(CLASSES) || echo -n 
	@[ -d BUILD ] && rm -rf BUILD || echo -n

install: install-lib install-links

install-lib: $(JARFILE)
	install -d $(LIBJARDIR)
	install -m 0644 $(JARFILE) $(LIBJARDIR)/librdanim-$(VERSION).jar

install-links:
	rm -f $(LIBJARDIR)/librdanim.jar
	ln -s $(LIBJARDIR)/librdanim-$(VERSION).jar \
		$(LIBJARDIR)/librdanim.jar
