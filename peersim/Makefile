VER=X.Y

.PHONY: all clean doc release

all:
	javac -classpath .:jep-2.24.jar `find -name "*.java"`
clean:
	rm -f `find -name "*.class"`
doc:
	rm -rf doc/*
	javadoc -classpath .:jep-2.24.jar -d doc \
		peersim \
		peersim.edsim \
		peersim.transport \
		peersim.core \
		peersim.cdsim \
		peersim.config \
		peersim.graph \
		peersim.util \
		peersim.reports \
		peersim.dynamics \
		example.loadbalance \
		example.newscast \
		example.aggregation \
		example.hot \
		peersim.vector

release: all doc
	rm -fr peersim-$(VER)
	mkdir peersim-$(VER)
	zip -r doc.zip doc
	mv doc.zip peersim-$(VER)
	rm -rf doc
	cp README CHANGELOG peersim-$(VER)
	mkdir peersim-$(VER)/example
	cp example/*.txt peersim-$(VER)/example
	jar cf peersim-$(VER).jar `find peersim example \( -name "*.java" -o -name "*.class" \)`
	mv peersim-$(VER).jar peersim-$(VER)

releaseall: all doc
	rm -fr peersim-$(VER)
	mkdir peersim-$(VER)
	mv doc peersim-$(VER)
	cp README CHANGELOG build.xml peersim-$(VER)
	mkdir peersim-$(VER)/example
	cp example/*.txt peersim-$(VER)/example
	mkdir peersim-$(VER)/src
	cp --parents `find peersim example -name "*.java"` peersim-$(VER)/src
	jar cf peersim-$(VER).jar `find peersim example -name "*.class"`
	mv peersim-$(VER).jar peersim-$(VER)
	cp jep-2.24.jar peersim-$(VER)
