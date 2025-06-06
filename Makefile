.PHONY: build

build:
	sbt -v compile
	sbt -v test:compile

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

.PHONY: test

test:
	sbt -v test

.PHONY: test-example

test-example:
	sbt -v "exampleJVM / runMain sporks.example.Example"
	sbt -v "exampleJVM / runMain sporks.example.LambdaExample"
	sbt -v "exampleJS / run"
	sbt -v "exampleNative / run"

.PHONY: sandbox

sandbox:
	rm -rf .sandbox
	mkdir -p .sandbox
	rsync -a . .sandbox --exclude='.sandbox'
	cd .sandbox && make clean && make build && make test && make test-example

VERSIONS = 3.3.4 \
	3.4.3 \
	3.5.2 \
	3.6.4

.PHONY: cross-build

cross-build:
	set -e; \
	for version in $(VERSIONS); do \
		sbt ++$${version}! -v compile && sbt ++$${version}! -v test:compile; \
	done

.PHONY: cross-test

cross-test:
	set -e; \
	for version in $(VERSIONS); do \
		sbt ++$${version}! -v test; \
	done

.PHONY: cross-test-example
cross-test-example:
	set -e; \
	for version in $(VERSIONS); do \
		sbt ++$${version}! -v "exampleJVM / runMain sporks.example.Example"; \
		sbt ++$${version}! -v "exampleJVM / runMain sporks.example.LambdaExample"; \
		sbt ++$${version}! -v "exampleJS / run"; \
		sbt ++$${version}! -v "exampleNative / run"; \
	done

.PHONY: cross-sandbox

cross-sandbox:
	rm -rf .sandbox
	mkdir -p .sandbox
	rsync -a . .sandbox --exclude='.sandbox'
	cd .sandbox && make clean && make cross-build && make cross-test && make cross-test-example
