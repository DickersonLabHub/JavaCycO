<?php
$listorgs = isset($_GET["listorgs"]) ? strtoupper($_GET["listorgs"]) : false;
$listpathways = isset($_GET["listpathways"]) ? strtoupper($_GET["listpathways"]) : false;
$org = isset($_GET["org"]) ? strtoupper($_GET["org"]) : false;
$pathway = isset($_GET["pathway"]) ? strtoupper($_GET["pathway"]) : false;
if(isset($_GET["clean"])) $clean = $_GET["clean"];
else $clean = "";

if($listorgs)
{
	$filename = "orgs.txt";
}
elseif($listpathways)
{
	if($org)
	{
		$filename = "$org:pathways.txt";
		if(!file_exists($filename) or $clean)
		{
			$cmd = ("grep $org pathways.txt > $filename");
			//echo "$cmd\n";
			exec($cmd);
		}
	}
	else
	{
		$filename = "pathways.txt";
	}
}
elseif((!$org and !$pathway) or strstr($org,";") or strstr($pathway,";"))
{
	include("index.php");
	exit;
}
elseif($org and $pathway)
{

	$filename = "$org:$pathway.xgmml";
	if(!file_exists($filename) or $clean)
	{
		$cmd = ("java -jar NetworkExporter.jar tht.vrac.iastate.edu 4444 $org $pathway xgmml biocyc-ecoli_at_vv_probeset_map.txt probeset > $filename");
		//echo "$cmd\n";
		exec($cmd);
	}

}

//header ("Content-Type: text/xml");
//header("Content-Disposition: attachment; filename=$filename");
//include($filename);
header("Location: http://vitis.student.iastate.edu/XGMML/$filename");

?>


