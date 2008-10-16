LZMA_SDK_VERSION = 4.57
LZMA_JIO_MAJOR = 0
LZMA_JIO_MINOR = 92
LZMA_JIO_VERSION = $(LZMA_JIO_MAJOR).$(LZMA_JIO_MINOR)

JAR = jar
JAR_FILE = lzma-$(LZMA_SDK_VERSION)-jio-$(LZMA_JIO_VERSION).jar
DIST_NAME = lzmajio-$(LZMA_JIO_VERSION)
DIST_FILE = $(DIST_NAME).tar.gz
AUX_FILES = Version.java

default:

all: build $(AUX_FILES)
	javac -d build $(shell find SevenZip -name '*.java') *.java

build:
	-mkdir build

jar: $(JAR_FILE)

$(JAR_FILE): all
	$(JAR) cf $@ CPL.html LGPL.txt -C build .

Version.java: Version.pl
	darcs changes $(REPODIR) --context \
	  | perl $< $(LZMA_JIO_MAJOR) $(LZMA_JIO_MINOR) >$@

predist: $(AUX_FILES)

dist: $(DIST_FILE)

$(DIST_FILE):
	REPODIR=--repodir=$$PWD darcs dist --dist-name $(DIST_NAME)

public: $(JAR_FILE) $(DIST_FILE)
	scp $^ comsci.liu.edu:public_html/dist/lzmajio

clean:
	$(RM) -r build $(AUX_FILES)

reallyclean: clean
	$(RM) $(JAR_FILE) $(DIST_FILE)

.PHONY: default all build jar predist dist public clean reallyclean
