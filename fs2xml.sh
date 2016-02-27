#!/bin/sh


function writeElement {
	dname=`echo "$1" | sed "s/.*\///g"`
	echo "<directory name='$dname'>"
	dir=$1
	fes=`find "$dir" -mindepth 1 -maxdepth 1 -type f $findExpress -printf "%f\n"`
	for i in $fes ; do
	 UU=""
	if [ "$stamp1" = "yes" ]; then 
		s=`stat -c %y $dir/$i`
		UU=$UU" datetime='$s' "
	 fi; 
	if [ "$stamp2" = "yes" ]; then 
		s=`stat -c %Y $dir/$i`
		UU=$UU" time='$s' "
	 fi; 
	if [ "$size" = "yes" ]; then 
		s=`stat --printf="%s" $dir/$i`
		UU=$UU" size='$s' "
	 fi; 
	 if [ "$md5" = "yes" ]; then 
		s=`md5sum $dir/$i | sed "s/ .*//g"`
		UU=$UU" md5='$s' "
	 fi; 
		echo "<file name='"$i"' $UU/>"

	done
	dis=`find "$dir" -mindepth 1 -maxdepth 1 -type d $findExpress`
	for i in $dis ; do
	writeElement $i
	done
	echo "</directory>"

}
size="no"
md5="no"
stamp1="no"
stamp2="no"
comment="no"
#Find *.txt file but ignore hidden .txt file such as .vimrc or .data.txt file:
# \( -iname "*.txt" ! -iname ".*" \)
#Find all .dot files but ignore .htaccess file:
# \( -iname ".*" ! -iname ".htaccess" \)
# eg ignore .hg will be ./fs2xml.sh f "! -iname .hg"
findExpress=""
next="none"
for var in "$@"
  do
  if [ "$comment" = "yes" ]; then 
    echo $var
  fi
  if [ "$next" = "f" ]; then 
    findExpress=$var; 
    next="none"
  fi;
  if [ "$var" = "s" ]; then size="yes"; fi;
  if [ "$var" = "m" ]; then md5="yes"; fi;
  if [ "$var" = "t" ]; then stamp1="yes"; fi;
  if [ "$var" = "tt" ]; then stamp2="yes"; fi;
  if [ "$var" = "c" ]; then comment="yes"; echo "<!--";echo "run in "`pwd`" with following args:";fi;
  if [ "$var" = "f" ]; then next="f"; fi;
done
if [ "$comment" = "yes" ]; then 
  echo "-->"
fi;
OLDIFS=$IFS
#IFS=$'\n' relict? why? 
writeElement `pwd`
IFS=$OLDIFS

