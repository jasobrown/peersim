all:
	latex tutorial1
	latex tutorial1

pdf:
	pdflatex tutorial1
	pdflatex tutorial1

ps: all
	dvips -t a4 -o tutorial1.ps tutorial1.dvi

html: all
	latex2html -t "Peersim HOWTO 1" -split 0 -nonavigation -noaddress -show_section_numbers -t "Peersim HOWTO 1" tutorial1.tex

clean:
	rm -f *.dvi *.aux *.log *.blg *.bbl *.ps tutorial1.pdf *.out *.toc

