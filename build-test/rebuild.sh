#!/bin/bash

rm -Rf ~/.ivy2/local/
rm -Rf ~/.ivy2/cache/pentaho/pentaho-modeler
rm -Rf ~/.ivy2/cache/pentaho/pentaho-metadata
cd ../../pentaho-metadata
ant clean-all resolve jar publish-local
cd ../modeler
ant clean-all resolve jar publish-local

