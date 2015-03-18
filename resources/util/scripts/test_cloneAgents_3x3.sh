cat 0\)\ 0-0.txt 0\)\ 0-1.txt 0\)\ 0-2.txt 0\)\ 1-0.txt 0\)\ 1-1.txt 0\)\ 1-2.txt 0\)\ 2-0.txt 0\)\ 2-1.txt 0\)\ 2-2.txt > start.txt
cat 99\)\ 0-0.txt 99\)\ 0-1.txt 99\)\ 0-2.txt 99\)\ 1-0.txt 99\)\ 1-1.txt 99\)\ 1-2.txt 99\)\ 2-0.txt 99\)\ 2-1.txt 99\)\ 2-2.txt > end.txt
sort end.txt > sortEnd.txt
sort start.txt > sortStart.txt
vimdiff sortEnd.txt sortStart.txt
