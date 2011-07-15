<?php if(file_exists("../header.php")) include("../header.php");?>

<h1>BioCyc - XGMML Webservice</h1>

<p>Use this service to obtain XGMML-formatted BioCyc pathway network structures.  XGMML is used by the software <a href="http://www.cytoscape.org">Cytoscape</a>.</p>
<h2>Usage:</h2>
<p>getXGMML.php?org=ORG&pathway=PATHWAY[&clean=1]</p>
<p></p>
<h2>Arguments:</h2>
<ol>
	<li><strong>listorgs</strong>  1 or 0.  List all the organisms available on this service.</li>
	<li><strong>listpathways</strong>  1 or 0.  List either all pathways on this service or all pathways under a specified organism by the <strong>org</strong> argument.</li>
	<li><strong>org</strong>  The BioCyc organism ID.  I currently serve up ECOLI, ARA, VITI, and META</li>
	<li><strong>pathway</strong>  The BioCyc pathway ID.  See <a href="http://www.biocyc.org">BioCyc.org</a> and navigate to your pathway to find its ID.  For example, <a href="http://biocyc.org/ECOLI/NEW-IMAGE?type=PATHWAY&object=GLYCOLYSIS">http://biocyc.org/ECOLI/NEW-IMAGE?type=PATHWAY&object=GLYCOLYSIS</a> shows that the BioCyc ID for the E. coli pathway Glycolysis is GLYCOLYSIS.</li>
	<li><strong>clean</strong>  Optionally re-retrieve the pathway data even if it has been cached (can take up to several minutes).</li>
</ol>
<p></p>
<h2>Some examples:</h2>
<ul>
	<li><a href="/XGMML/getXGMML.php?listorgs=1">List all the organisms available on this service.</a>
	<li><a href="/XGMML/getXGMML.php?org=ECOLI&listpathways=1">List all ECOLI pathways on this service.</a>
	<li><a href="/XGMML/getXGMML.php?org=ECOLI&pathway=GLYCOLYSIS">The EcoCyc glycolyis pathway</a>
	<li><a href="/XGMML/getXGMML.php?org=ARA&pathway=PWY-2541">The AraCyc pathway "plant sterol biosynthesis"</a>
</ul>
<p></p>
<h2>See also: <a href="http://www.plexdb.org/modules/webservices">PLEXdb data webservices</a></h2>

<?php if(file_exists("../footer.php")) include("../footer.php");?>
