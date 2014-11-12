#!/bin/bash

rm -Rf output
mkdir output
cd ..
ant clean-all resolve

SUCCESS_COUNT=0

for ((i=1;i<=1000;i++));
do
  echo -n "Iteration $i: "
  ant clean gwt-compile.validate &> "build-test/output/$i.txt"
  SUCCESS=`grep "BUILD SUCCESSFUL" build-test/output/$i.txt`
  if [ "$SUCCESS" = "BUILD SUCCESSFUL" ] ; then
    echo -n "PASS   "
    SUCCESS_COUNT=$(($SUCCESS_COUNT + 1))
  else
    echo -n "FAIL   "
    exit 0
  fi

  SUCCESS_RATE="  0.000"
  if [ $SUCCESS_COUNT -gt 0 ] ; then
    SUCCESS_RATE=`bc <<< "scale=3; (($SUCCESS_COUNT/$i)*100)"`
  fi
  echo " SUCCESS RATE: $SUCCESS_RATE%"
done
