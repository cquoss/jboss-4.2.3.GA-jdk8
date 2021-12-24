# this script generates the cruise control config.xml files from the template

. generate.conf

edit_file()
{
sedcmd=$1
sedfile=$2
sedtofile=${3:-$sedfile}

#echo sed $sedcmd $sedfile $sedtofile

sed $sedcmd $sedfile >$sedtofile.tmp
mv $sedtofile.tmp $sedtofile
}

merge_template()
{
. $1

rm $PROJECTCC

edit_file s/PROJECTNAME/$PROJECTNAME/g config.xml.template $PROJECTCC
edit_file s/BUILDTARGET/$BUILDTARGET/g $PROJECTCC
edit_file s/EMAILTO/$EMAILTO/g $PROJECTCC
edit_file s/EMAILFROM/$EMAILFROM/g $PROJECTCC
edit_file s/WEBHOST/$WEBHOST/g $PROJECTCC
edit_file s*CCDIR*$CCDIR*g $PROJECTCC
edit_file s/MAILSERVER/$MAILSERVER/g $PROJECTCC
edit_file s/CVSTAGATTRIBUTE/$CVSTAGATTRIBUTE/g $PROJECTCC

cat $PROJECTCC
}

echo "<cruisecontrol>"          > cc-config-all.xml

merge_template build-head.conf >> cc-config-all.xml
merge_template build-3.2.conf  >> cc-config-all.xml
merge_template build-3.0.conf  >> cc-config-all.xml

echo "</cruisecontrol>"        >> cc-config-all.xml
