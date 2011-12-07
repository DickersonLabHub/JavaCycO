#!/bin/bash

#You can run this to recreate the cache.  Will first delete the existing files, the call on NetworkExporter to recreate them.  This is useful if you 
#anticipate that the underlying data has changed since the last time the files were created, but don't want to call each pathway individually to have its
#xgmml file recreated.

ORG=ARA
rm -f $ORG*.xgmml
java -jar NetworkExporter.jar tht.vrac.iastate.edu 4444 $ORG refreshCache xgmml biocyc-ecoli_at_vv_probeset_map.txt probeset