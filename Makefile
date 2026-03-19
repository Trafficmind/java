.PHONY: test verify build run-example

test:
	mvn -q test

verify:
	mvn -q verify

build:
	mvn -q -DskipTests package

run-example:
	mvn -q exec:java
