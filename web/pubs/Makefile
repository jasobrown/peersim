.PHONY: all htmlfiles publish clean cleanall

all: htmlfiles

htmlfiles: \
  desc.html

references:
	mkdir -p refs
	rm -f refs/*
	cd refs ; pwd ; awk -f ../bib-split.awk ../references.bib
	
%.html: %.tex peersim.bib is-unsrt-web.bst
	htlatex `basename $< .tex` "html,uni-html4" " -cmozhtf"
	bibtex `basename $< .tex`
	htlatex `basename $< .tex` "html,uni-html4" " -cmozhtf"
#	mv `basename $< .tex`.html `basename $< .tex`.shtml

publish: htmlfiles clean
	pubblica-web

cleanall: clean
	rm -rf refs *.shtml *.css > /dev/null

clean:
	rm -rf WARNINGS *.out *.aux *.dvi *.log *.bbl *.blg *.pl *.4ct *.4tc *.idv *.lg *.html *.xref *.tmp > /dev/null

