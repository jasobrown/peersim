.PHONY: all htmlfiles publish clean cleanall

all: references htmlfiles

htmlfiles: \
  desc.html 

references:
	mkdir -p refs
	rm -f refs/*
	cd refs ; pwd ; awk -f ../bib-split.awk ../peersim.bib
	
%.html: %.tex peersim.bib alpha-web.bst
	latex `basename $< .tex`
	bibtex `basename $< .tex`
	latex `basename $< .tex`
	latex `basename $< .tex`
	latex2html -nosubdir -no_auto_link -html_version 4.0 -info 0 -split 0 -no_navigation $<

publish: htmlfiles clean
	pubblica-web

cleanall: clean
	rm -rf refs *.html *.css > /dev/null

clean:
	rm -rf WARNINGS *.out *.aux *.dvi *.log *.bbl *.blg *.pl > /dev/null

