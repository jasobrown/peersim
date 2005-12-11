all:
	latex tutorial2
	latex tutorial2

pdf:
	pdflatex tutorial2
	pdflatex tutorial2

ps: all
	dvips -t a4 -o tutorial2.ps tutorial2.dvi

html: all
	latex2html -t "Peersim HOWTO 2" -split 0 -nonavigation -noaddress -show_section_numbers -t "Peersim HOWTO 2" tutorial2.tex

clean:
	rm -f *.dvi *.aux *.log *.blg *.bbl *.ps tutorial2.pdf *.out *.toc

