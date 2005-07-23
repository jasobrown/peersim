VER=0.4

.PHONY: all clean doc release

all:
	javac -classpath .:jep-2.3.0.jar:djep-1.0.0.jar `find -name "*.java"`
clean:
	rm -f `find -name "*.class"`
doc:
	rm -rf doc/*
	javadoc -classpath .:jep-2.3.0.jar:djep-1.0.0.jar -d doc \
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
		peersim.vector \
		example.loadbalance \
		example.newscast \
		example.aggregation \
		example.hot 

docnew:
	rm -rf doc/*
	javadoc -docletpath /home/montreso/Workspace/Doclet/classes -doclet doclets.standard.Standard -classpath .:jep-2.3.0.jar:djep-1.0.0.jar -d doc \
                -group "Peersim" "peersim*" \
                -group "Examples" "example.*" \
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
		peersim.vector \
		example.loadbalance \
		example.newscast \
		example.aggregation \
		example.hot 
#	javadoc -docletpath peersim-doclet.jar -doclet doclets.standard.Standard -classpath .:jep-2.3.0.jar:djep-1.0.0.jar -d doc \

release: all doc
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
	cp jep-2.3.0.jar peersim-$(VER)
	cp djep-1.0.0.jar peersim-$(VER)
