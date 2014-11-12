#!/bin/bash

rm -Rf ~/.ivy2/local/
rm -Rf ~/.ivy2/cache/pentaho/pentaho-modeler
rm -Rf ~/.ivy2/cache/pentaho/pentaho-metadata
cd ../../pentaho-metadata-cleanup
ant clean-all resolve jar publish-local
cd ../modeler-cleanup
ant clean-all resolve jar publish-local
cd ../data-access-cleanup

