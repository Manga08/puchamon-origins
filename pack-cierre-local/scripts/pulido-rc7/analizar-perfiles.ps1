param([datetime]$Since=[datetime]'2026-10-14T20:00:00')
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$folder=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft/debug/profiling'
function ReadEntry($zip,$name){$entry=$zip.GetEntry($name);if(!$entry){return $null};$reader=[IO.StreamReader]::new($entry.Open());try{return $reader.ReadToEnd()}finally{$reader.Dispose()}}
$rows=@()
foreach($file in Get-ChildItem -LiteralPath $folder -Filter '*.zip' | Where-Object LastWriteTime -GE $Since | Sort-Object LastWriteTime){
 $zip=[IO.Compression.ZipFile]::OpenRead($file.FullName)
 try{
  $ticks=@((ReadEntry $zip 'client/metrics/ticking.csv') | ConvertFrom-Csv | ForEach-Object {[double]::Parse($_.ticktime,[Globalization.CultureInfo]::InvariantCulture)/1e6} | Sort-Object)
  if(!$ticks.Count){continue}
  $heap=@((ReadEntry $zip 'client/metrics/jvm.csv') | ConvertFrom-Csv | ForEach-Object {[double]::Parse($_.'heap MiB',[Globalization.CultureInfo]::InvariantCulture)})
  $rows+=[ordered]@{file=$file.Name;writtenAt=$file.LastWriteTime.ToString('o');clientFrames=$ticks.Count;frameMedianMs=$ticks[[int][math]::Floor($ticks.Count*.5)];frameP95Ms=$ticks[[int][math]::Floor($ticks.Count*.95)];frameP99Ms=$ticks[[int][math]::Floor($ticks.Count*.99)];frameMaxMs=$ticks[-1];framesOver50ms=@($ticks | Where-Object {$_ -gt 50}).Count;heapMaxMiB=($heap | Measure-Object -Maximum).Maximum}
 }finally{$zip.Dispose()}
}
$rows | ConvertTo-Json -Depth 8 | Set-Content "$project/pulido-rc7/evidencias/perfiles-cliente.json" -Encoding utf8NoBOM
$rows | ConvertTo-Json -Depth 8
