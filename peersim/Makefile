all:
	javac `find -name "*.java"`
clean:
	rm -f `find -name "*.class"`
doc:
	rm -rf doc/*
	javadoc -overview overview.html -d doc \
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
			
