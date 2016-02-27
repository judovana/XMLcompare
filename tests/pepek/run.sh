DIFDIR=difs
LOGSDIR=logs
START=$(date +%s);
for x1 in 1 2 3  
 do

	for x2 in 1 2 3  
	 do

            if [ $x1 = $x2 ]
	     then
            
	         continue
		
	      fi
		   n1="wst$x1.xml"
		   n2="wst$x2.xml"
		   name=$n1"_x_"$n2
		   #java -Xmx1g -jar ../../dist/XMLcompare.jar  -dual -orphanseek -ignoreorder=2  $n1 $n2 -forceTrim -diff=$DIFDIR/$name.2.xdiff > $LOGSDIR/$name.2.log
		   #java -Xmx1g -jar ../../dist/XMLcompare.jar  -dual -orphanseek -ignoreorder=1  $n1 $n2 -forceTrim -diff=$DIFDIR/$name.1.xdiff > $LOGSDIR/$name.1.log
		   # meld $n1 $n2 &
		   java -Xmx1g -jar ../../dist/XMLcompare.jar  -dual -orphanseek -ignoreorder=2  $n1 $n2 -forceTrim -visualise
		   java -Xmx1g -jar ../../dist/XMLcompare.jar  -dual -orphanseek -ignoreorder=2  -ignoreUri $n1 $n2 -forceTrim -visualise
		   q1=`cat $LOGSDIR/$name.1.log | grep "Total errors"`
	 	   q2=`cat $LOGSDIR/$name.1.log | grep "XMLs are same"`
		   echo "$n1 x $n2 "$q1$q2

	  done

 done
END=$(date +%s);
echo ""
DIFF="$(( $END - $START ))"
echo $DIFF"seconds"

