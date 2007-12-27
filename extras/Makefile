.PHONY: all clean doc compile

PEERSIM_JARS=""
LIB_JARS=`find -L lib/ -name "*.jar" | tr [:space:] :`

compile:
	mkdir -p classes
	javac -sourcepath src -classpath $(LIB_JARS):$(PEERSIM_JARS) -d classes `find -L src/ -name "*.java"`

doc:
	mkdir -p doc
	javadoc -sourcepath src/ -classpath $(LIB_JARS):$(PEERSIM_JARS) -d doc -subpackages peersim.extras.am.epidemic

clean: 
	rm -fr classes doc