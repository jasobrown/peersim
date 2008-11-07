BEGIN { FS="[{,]" }
/^@/ { output=$2 ".bib" ; print output ; if (output != null) printf "" > output }
{ if (output != null) print $0 >> output}

