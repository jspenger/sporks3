.PHONY: clean

clean:
	find . -name .DS_Store | xargs rm -fr
	find . -name .metals | xargs rm -fr
	find . -name .vscode | xargs rm -fr
	find . -name .idea | xargs rm -fr
	find . -name .bsp | xargs rm -fr
	find . -name .bloop | xargs rm -fr
	find . -name "*.json" | xargs rm -fr
	find . -name metals.sbt | xargs rm -fr
	rm -fr project/project/
	find . -name target | xargs rm -fr

.PHONY: build

build:
	sbt -v compile
	sbt -v test:compile

.PHONY: test

test:
	sbt -v test

.PHONY: test-example

test-example:
	sbt -v "exampleJVM / runMain sporks.example.BuilderExample"
	sbt -v "exampleJVM / runMain sporks.example.LambdaExample"
	sbt -v "exampleJS / run"
	sbt -v "exampleNative / run"

.PHONY: sandbox

sandbox:
	rm -rf .sandbox
	mkdir -p .sandbox
	rsync -a . .sandbox --exclude='.sandbox'
	cd .sandbox && make clean && make build && make test && make test-example
