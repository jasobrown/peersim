all:
#	latex all
#	bibtex all
	latex tutorial2
	latex tutorial2

pdf: all
	dvipdfm tutorial2.dvi

ps: all
	dvips -t a4 -o tutorial2.ps tutorial2.dvi

html: all
	latex2html -t "Peersim HOWTO 2" -split 0 -nonavigation -noaddress -show_section_numbers -t "Peersim HOWTO 2" tutorial2.tex

clean:
	rm -f *.dvi *.aux *.log *.blg *.bbl *.ps *.pdf *.out *.toc

