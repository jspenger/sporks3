.PHONY: build

build:
	sbt -Dsbt.server=false -v compile
	sbt -Dsbt.server=false -v test:compile

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
	sbt -Dsbt.server=false -v test

.PHONY: test-example

test-example:
	sbt -Dsbt.server=false -v "exampleJVM / runMain sporks.example.Example"
	sbt -Dsbt.server=false -v "exampleJVM / runMain sporks.example.LambdaExample"
	sbt -Dsbt.server=false -v "exampleJS / run"
	sbt -Dsbt.server=false -v "exampleNative / run"

.PHONY: sandbox

sandbox:
	rm -rf .sandbox
	mkdir -p .sandbox
	rsync -a . .sandbox --exclude='.sandbox'
	cd .sandbox && $(MAKE) clean && $(MAKE) build && $(MAKE) test && $(MAKE) test-example

VERSIONS = 3.3.6 \
	3.4.3 \
	3.5.2 \
	3.6.4 \
	3.7.1

.PHONY: cross-build

cross-build:
	set -e -x -o pipefail; \
	for version in $(VERSIONS); do \
		sbt -Dsbt.server=false ++$${version}! -v compile; \
		sbt -Dsbt.server=false ++$${version}! -v test:compile; \
	done

.PHONY: cross-test

cross-test:
	set -e -x -o pipefail; \
	for version in $(VERSIONS); do \
		sbt -Dsbt.server=false ++$${version}! -v test; \
	done

.PHONY: cross-test-example

cross-test-example:
	set -e -x -o pipefail; \
	for version in $(VERSIONS); do \
		sbt -Dsbt.server=false ++$${version}! -v "exampleJVM / runMain sporks.example.Example"; \
		sbt -Dsbt.server=false ++$${version}! -v "exampleJVM / runMain sporks.example.LambdaExample"; \
		sbt -Dsbt.server=false ++$${version}! -v "exampleJS / run"; \
		sbt -Dsbt.server=false ++$${version}! -v "exampleNative / run"; \
	done

.PHONY: cross-sandbox

cross-sandbox:
	rm -rf .sandbox
	mkdir -p .sandbox
	rsync -a . .sandbox --exclude='.sandbox'
	cd .sandbox && $(MAKE) clean && $(MAKE) cross-build && $(MAKE) cross-test && $(MAKE) cross-test-example

JVM_VERSIONS = 8.0.452-zulu 11.0.27-tem 17.0.15-tem 21.0.7-tem

.PHONY: paranoid

paranoid:
	set -e -x -o pipefail; \
	for jvm in $(JVM_VERSIONS); do \
		echo "Testing with JAVA_HOME for JDK $${jvm}"; \
		. "$$HOME/.sdkman/bin/sdkman-init.sh"; \
		sdk use java $${jvm}; \
		set -e -x -o pipefail; \
		java -version; \
		$(MAKE) clean; \
		$(MAKE) cross-sandbox; \
	done
