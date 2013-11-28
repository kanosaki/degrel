
.PHONY: init test

init:
	git submodule update --init

test:
	./sbt test
