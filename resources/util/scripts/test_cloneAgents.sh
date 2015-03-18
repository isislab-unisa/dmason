cat 0\)\ 0-0.txt 0\)\ 0-1.txt 0\)\ 1-0.txt 0\)\ 1-1.txt > start.txt
cat 99\)\ 0-0.txt 99\)\ 0-1.txt 99\)\ 1-0.txt 99\)\ 1-1.txt > end.txt
sort end.txt > sortEnd.txt
sort start.txt > sortStart.txt
vimdiff sortEnd.txt sortStart.txt
