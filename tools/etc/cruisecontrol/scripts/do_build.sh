#!/bin/bash

# $Id: do_build.sh 20595 2004-04-20 21:13:14Z kimptoc $

. common.shlib

log ======================================
log "BEGIN:" `date` - $0
log parameters: $*

CVSCO=${1:-Y}

CVSBRANCH=${2:-HEAD}
CVSMODULE=${3:-jboss-head}

setuplogdir $ROOTDIR/logbuild/$CVSMODULE

BUILD_RET_CODE=0

log OS VERSION INFO
uname -a

CVSDIR=../checkout/

if [ ! -d $CVSDIR ]; then
  mkdir $CVSDIR
fi


log CVSDIR = $CVSDIR

# can specify multiple jvms here and pass into CONFIG0 as a space separated list

export JJ=${J1:-/cygdrive/c/j2sdk1.4.2_03}

#JAVA_HOMES=$CONFIG0

log get jboss from cvs,

if [ "$CVSCO" != "N" ]; then
  cvsco $CVSBRANCH $CVSMODULE $CVSDIR
else
  log CVS checkout disabled - must be testing something...
fi

export PATH_ORIG="$PATH"

#log java homes = $JAVA_HOMES
#let COMPILECOUNT=1
#for JAVA_HOMEX in $JAVA_HOMES ;
#do
#JAVA_HOMEX=$JAVA_HOMES
  log Compiling jboss with $J1
  compile_jboss 0 $JJ $CVSDIR $CVSMODULE
  BUILD_RET_CODE=$?
#  let COMPILECOUNT++
#done;

log "END:  " `date` "-" $0 "- return code: " $BUILD_RET_CODE 
log ======================================

exit $BUILD_RET_CODE
