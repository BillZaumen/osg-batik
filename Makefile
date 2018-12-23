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
SYS_BZDEV_DIR = /usr/share/bzdev

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

BATIK_CLASS_DIR = BUILD/osgmod

BZDEV_MODULES = base desktop
BZDEV_JARS = $(shell for i in $(BZDEV_MODULES); \
		do echo BUILD/libbzdev-$$i.jar ; done)


EXTLIBS=BUILD/libbzdev-base.jar:BUILD/libbzdev-desktop.jar

ALL = jarfile

all: $(ALL)

# Target for the standard Java extension directory
LIBJARDIR = $(DESTDIR)$(SYS_LIBJARDIR)
BZDEV_DIR = $(DESTDIR)$(SYS_BZDEV_DIR)

# Other target directories

API_DOCDIR = $(DESTDIR)$(SYS_API_DOCDIR)

JROOT_JARDIR = $(JROOT)/BUILD
JROOT_LIBJARDIR = $(JROOT_JARDIR)

JDOCS = *.html stylesheet.css package-list
RDOCS = *.gif


BZDEV_JARS = $(shell for i in $(BZDEV_MODULES); \
		do echo BUILD/libbzdev-$$i.jar ; done)

all: $(JARFILE)


# We don't seem to need the service providers that Batik supports.
$(BATIK_CLASS_DIR): $(BATIK_JARS)
	mkdir -p BUILD
	rm -rf BUILD/osgmod-tmp
	mkdir BUILD/osgmod-tmp
	for i in $(BATIK_JARS) ; \
		do unzip -o -d BUILD/osgmod-tmp $$i > /dev/null; done
	rm -rf BUILD/osgmod-tmp/org/w3c
	rm -rf BUILD/osgmod-tmp/META-INF/maven
	rm -rf BUILD/osgmod-tmp/META-INF/MANIFEST.MF
	mv BUILD/osgmod-tmp BUILD/osgmod

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

$(JARFILE): $(OSGBATIK_JFILES) $(BZDEV_JARS) $(BATIK_CLASS_DIR) \
	$(OSGBATIK_DIR)/module-info.java \
	$(OSGBATIK_DIR)/META-INF/services/org.bzdev.gio.spi.OSGProvider
	mkdir -p mods/org.bzdev.osgbatik
	mkdir -p BUILD
	javac -d mods/org.bzdev.osgbatik -p $(EXTLIBS) \
		--patch-module org.bzdev.osgbatik=$(BATIK_CLASS_DIR) \
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
		copyright -C mods/org.bzdev.osgbatik . \
		-C BUILD/osgmod .

clean:
	@[ -d mods ] && rm -rf mods || echo -n 
	@[ -d BUILD ] && rm -rf BUILD || echo -n

install: install-lib install-links

install-lib: $(JARFILE)
	install -d $(LIBJARDIR)
	install -m 0644 $(JARFILE) $(LIBJARDIR)/libosgbatik-$(VERSION).jar

install-links: $(JARFILE)
	rm -f $(LIBJARDIR)/libosgbatik.jar
	ln -s $(LIBJARDIR)/libosgbatik-$(VERSION).jar \
		$(LIBJARDIR)/libosgbatik.jar
	install -d $(BZDEV_DIR)
	rm -f $(BZDEV_DIR)/lisbosgbatik.jar
	ln -s $(LIBJARDIR)/libosgbatik.jar \
		$(BZDEV_DIR)/libosgbatik.jar
