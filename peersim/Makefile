VER=X.Y

.PHONY: all clean doc release

all:
	javac -classpath .:jep-2.24.jar `find -name "*.java"`
clean:
	rm -f `find -name "*.class"`
doc:
	rm -rf doc/*
	javadoc -classpath .:jep-2.24.jar -overview overview.html -d doc \
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
		distributions
	#javadoc -overview overview.html -d doc peersim.core peersim.cdsim peersim.config peersim.graph peersim.util peersim.reports peersim.dynamics newscast aggregation scamp lpbcast dpvem lbalance myaggreg

release: all doc
	rm -fr peersim-$(VER)
	mkdir peersim-$(VER)
	zip -r doc.zip doc
	mv doc.zip peersim-$(VER)
	rm -rf doc
	cp README CHANGELOG peersim-$(VER)
	mkdir peersim-$(VER)/example
	cp example/*.txt peersim-$(VER)/example
	jar cf peersim-$(VER).jar `find peersim distributions example \( -name "*.java" -o -name "*.class" \)`
	mv peersim-$(VER).jar peersim-$(VER)


