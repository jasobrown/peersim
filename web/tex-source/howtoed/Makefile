all:
	latex all
	latex all

pdf:
	pdflatex all
	pdflatex all

ps: all
	dvips -t a4 -o all.ps all.dvi

html: all
	latex2html -t "Peersim HOWTO 1" -split 0 -nonavigation -noaddress -show_section_numbers -t "Peersim HOWTO 1" all.tex

clean:
	rm -f *.dvi *.aux *.log *.blg *.bbl *.ps all.pdf *.out *.toc

