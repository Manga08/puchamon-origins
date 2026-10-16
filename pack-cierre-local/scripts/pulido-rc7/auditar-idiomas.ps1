param([string]$Minecraft=(Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'))
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$out=Join-Path $project 'pulido-rc7/idiomas-20261016'
New-Item -ItemType Directory -Force $out | Out-Null
$english=@{};$spanish=@{};$origins=@{};$issues=[Collections.Generic.List[object]]::new();$archives=0
function Read-Languages($Zip,[string]$Origin){
 foreach($entry in $Zip.Entries){
  if($entry.FullName -match '^assets/([^/]+)/lang/(en_us|es_es)\.json$'){
   $ns=$matches[1];$locale=$matches[2];$reader=[IO.StreamReader]::new($entry.Open(),[Text.Encoding]::UTF8)
   try{$raw=$reader.ReadToEnd();$values=ConvertFrom-Json -InputObject $raw -AsHashtable}catch{$issues.Add(@{kind='invalid-json';origin=$Origin;entry=$entry.FullName;error=$_.Exception.Message});continue}finally{$reader.Dispose()}
   if(!$english.ContainsKey($ns)){$english[$ns]=@{};$spanish[$ns]=@{};$origins[$ns]=[Collections.Generic.List[string]]::new()}
   $origins[$ns].Add($Origin)
   $map=if($locale -eq 'en_us'){$english[$ns]}else{$spanish[$ns]}
   foreach($key in $values.Keys){$map[$key]=$values[$key]}
  }elseif($entry.FullName -match '^META-INF/jars/[^/]+\.jar$'){
   $memory=[IO.MemoryStream]::new();$stream=$entry.Open();try{$stream.CopyTo($memory)}finally{$stream.Dispose()};$memory.Position=0
   $inner=[IO.Compression.ZipArchive]::new($memory,[IO.Compression.ZipArchiveMode]::Read)
   try{Read-Languages $inner "$Origin!$($entry.FullName)"}finally{$inner.Dispose();$memory.Dispose()}
  }
 }
}
$mods=Get-ChildItem -LiteralPath "$Minecraft/mods" -Filter '*.jar' -File | Sort-Object Name
foreach($file in $mods){$zip=[IO.Compression.ZipFile]::OpenRead($file.FullName);try{Read-Languages $zip $file.Name;$archives++}finally{$zip.Dispose()}}
$selection=(Get-Content -LiteralPath "$Minecraft/options.txt" | Where-Object {$_ -like 'resourcePacks:*'}).Substring(14) | ConvertFrom-Json
foreach($pack in $selection | Where-Object {$_ -like 'file/*'}){
 $file=Join-Path "$Minecraft/resourcepacks" $pack.Substring(5)
 if((Test-Path -LiteralPath $file -PathType Leaf) -and $file.EndsWith('.zip')){$zip=[IO.Compression.ZipFile]::OpenRead($file);try{Read-Languages $zip $pack;$archives++}finally{$zip.Dispose()}}
}
# Apply own narrow overlays last for the candidate coverage audit; runtime order still needs verification.
foreach($name in @(Get-ChildItem -LiteralPath "$Minecraft/mods" -File | Where-Object {$_.Name -match '^puchamon-(translations|ui-polish)-.+\.jar$'} | Select-Object -ExpandProperty Name)){
 $zip=[IO.Compression.ZipFile]::OpenRead("$Minecraft/mods/$name");try{Read-Languages $zip $name}finally{$zip.Dispose()}
}
$rows=[Collections.Generic.List[object]]::new();$details=@{}
foreach($ns in $english.Keys | Sort-Object){
 $missing=@();$same=@();$damaged=@();$tokens=@()
 foreach($key in $english[$ns].Keys | Sort-Object){
  $en=[string]$english[$ns][$key]
  if(!$spanish[$ns].ContainsKey($key)){$missing+=@{key=$key;english=$en};continue}
  $es=[string]$spanish[$ns][$key]
  if($en -eq $es){$same+=@{key=$key;value=$es}}
  if($es.Contains([char]0xfffd) -or $es -match '[A-Za-zÁÉÍÓÚáéíóúñÑ]\?[A-Za-zÁÉÍÓÚáéíóúñÑ]|Ã.|Â[¿¡ ]'){$damaged+=@{key=$key;english=$en;spanish=$es}}
  $et=@([regex]::Matches($en,'%(?:\d+\$)?[sd]|\$\{[^}]+\}') | ForEach-Object Value | Sort-Object)
  $st=@([regex]::Matches($es,'%(?:\d+\$)?[sd]|\$\{[^}]+\}') | ForEach-Object Value | Sort-Object)
  if(($et -join '|') -ne ($st -join '|')){$tokens+=@{key=$key;english=$en;spanish=$es}}
 }
 $row=[pscustomobject][ordered]@{namespace=$ns;english=$english[$ns].Count;spanish=$spanish[$ns].Count;missing=$missing.Count;identical=$same.Count;suspectEncoding=$damaged.Count;placeholderDifferences=$tokens.Count;origins=@($origins[$ns] | Sort-Object -Unique)}
 $rows.Add($row);$details[$ns]=@{summary=$row;missing=$missing;identical=$same;encoding=$damaged;placeholders=$tokens;english=$english[$ns];spanish=$spanish[$ns]}
}
$details | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath "$out/detalle-idiomas.json" -Encoding utf8NoBOM
@{archives=$archives;namespaces=$rows.Count;rows=$rows;issues=$issues;limits=@('Static coverage only: identical strings can be proper names, not necessarily untranslated.','Spanish native text can be inaccurate even if coverage is complete.','Built-in optional resource packs and runtime-generated/server messages need separate checks.','Own overlays evaluated last; verify effective text in the running client before final release.')} | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath "$out/resumen-idiomas.json" -Encoding utf8NoBOM
$rows | Sort-Object missing -Descending | Select-Object namespace,english,spanish,missing,identical,suspectEncoding,placeholderDifferences | Format-Table -AutoSize | Out-String | Write-Output
Write-Output "Audit only; no instance files changed. Evidence: $out"
