default:

JFILES := $(shell find SevenZip net -name '*.java')

all: build
	javac -d build $(JFILES)

build:
	mkdir build
