LZMA_SDK_VERSION = 4.43
LZMA_JIO_MAJOR = 0
LZMA_JIO_MINOR = 7
LZMA_JIO_VERSION = $(LZMA_JIO_MAJOR).$(LZMA_JIO_MINOR)

JAR = jar
JAR_FILE = lzma-$(LZMA_SDK_VERSION)-jiostream-$(LZMA_JIO_VERSION).jar
DIST_FILE = lzma-jiostream-$(LZMA_JIO_VERSION)
AUX_FILES = Version.java

default:

all: build $(AUX_FILES)
	javac -d build $(shell find SevenZip -name '*.java') *.java

build:
	mkdir build

jar: $(JAR_FILE)

$(JAR_FILE): all
	$(JAR) cf $@ CPL.html LGPL.txt -C build .

Version.java: Version.pl
	darcs changes $(REPODIR) --context \
	  | perl $< $(LZMA_JIO_MAJOR) $(LZMA_JIO_MINOR) >$@

predist: $(AUX_FILES)

dist:
	REPODIR=--repodir=$$PWD darcs dist --dist-name $(DIST_FILE)

clean:
	$(RM) -r build $(AUX_FILES)

reallyclean: clean
	$(RM) $(JAR_FILE) $(DIST_FILE).tar.gz
