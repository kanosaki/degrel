
.PHONY: init test run

run:
	java -jar target/scala-2.10/degrel.jar $*

init:
	git submodule update --init

test:
	./sbt test
