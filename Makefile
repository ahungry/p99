default: help

all: build

test:
	lein test

# export _JAVA_OPTIONS='-Dawt.useSystemAAFontSettings=lcd'
run: target/uberjar/p99-0.1.0-SNAPSHOT-standalone.jar
	java \
		-Dswing.aatext=true \
		-Dawt.useSystemAAFontSettings=lcd \
		-jar ./target/uberjar/p99-0.1.0-SNAPSHOT-standalone.jar

target/uberjar/p99-0.1.0-SNAPSHOT-standalone.jar:
	lein deps
	lein uberjar

build:
	lein uberjar

uberjar: build

list-deps:
	lein deps :tree

help:
	$(info Hint: Run something like - make build && make test && make run)
	$(info Available commands: [ all | test | run | build | list-deps | help ])

.PHONY: uberjar build test help list-deps
