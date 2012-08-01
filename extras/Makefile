.PHONY: all clean doc

PEERSIM_JARS=""
LIB_JARS=`find -L lib/ -name "*.jar" | tr [:space:] :`

all:
	mkdir -p classes
	javac -sourcepath src -classpath $(LIB_JARS):$(PEERSIM_JARS) -d classes `find -L src/ -name "*.java"`

doc:
	mkdir -p doc
	javadoc -sourcepath src/ -classpath $(LIB_JARS):$(PEERSIM_JARS) -d doc -subpackages peersim.extras.am.epidemic

clean: 
	rm -fr classes doc

release: clean all 
	rm -rf extras
	mkdir extras
	cp Makefile README LICENSE extras
	mkdir extras/src
	mkdir extras/lib
	cp --parents `find src -name "*.java"` extras/src
	cd classes ; jar cf ../extras.jar `find . 	-name "*.class"`
	mv extras.jar extras/lib
	cp lib/*.jar extras/lib
