<?php
$org = isset($_GET["org"]) ? strtoupper($_GET["org"]) : false;
$pathway = isset($_GET["pathway"]) ? strtoupper($_GET["pathway"]) : false;
if(isset($_GET["clean"])) $clean = $_GET["clean"];
else $clean = "";

if(!$org or !$pathway or strstr($org,";") or strstr($pathway,";"))
{
	include("index.php");
}
else
{

	$filename = "$org:$pathway.xgmml";
	if(!file_exists($filename) or $clean)
	{
		$cmd = ("java -jar NetworkExporter.jar vitis.student.iastate.edu 4444 $org $pathway xgmml biocyc-ecoli_at_vv_probeset_map.txt probeset > $filename");
		//echo "$cmd\n";
		exec($cmd);
	}
	header ("Content-Type: text/xml");
	header("Content-Disposition: attachment; filename=$filename");
	include($filename);
}
?>


