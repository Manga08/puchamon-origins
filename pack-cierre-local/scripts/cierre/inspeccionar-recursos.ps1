$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$mods=Join-Path $project 'prueba-social-r3/server/mods'
$out=Join-Path $project 'cierre-rc6/evidencias'
New-Item -ItemType Directory -Force -Path $out | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$rows=@()
foreach($pattern in @('*footblock*','*confetti*','*beachparty*','*immersive_paintings*','*SecurityCraft*','*essential_commands*')){
 foreach($file in Get-ChildItem -LiteralPath $mods -Filter $pattern -File){
  $zip=[IO.Compression.ZipFile]::OpenRead($file.FullName)
  try{
   foreach($entry in $zip.Entries | Where-Object FullName -Match '^assets/[^/]+/lang/en_us.json$'){
    $reader=[IO.StreamReader]::new($entry.Open());try{$obj=$reader.ReadToEnd() | ConvertFrom-Json -AsHashtable}finally{$reader.Dispose()}
    $ns=$entry.FullName.Split('/')[1]
    $spanish=@{};$entryEs=$zip.GetEntry("assets/$ns/lang/es_es.json")
    if($entryEs){$readerEs=[IO.StreamReader]::new($entryEs.Open());try{$spanish=$readerEs.ReadToEnd() | ConvertFrom-Json -AsHashtable}finally{$readerEs.Dispose()}}
    $rows+=@{jar=$file.Name;ns=$ns;english=$obj;spanish=$spanish;hasSpanish=($null -ne $entryEs);recipes=@($zip.Entries | Where-Object FullName -Match '^data/[^/]+/recipe/[^/]+.json$' | ForEach-Object FullName)}
   }
  }finally{$zip.Dispose()}
 }
}
$rows | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath "$out/lenguajes-sociales.json" -Encoding utf8NoBOM
$rows | ForEach-Object {"$($_.ns): $($_.english.Count) keys; Spanish=$($_.hasSpanish)"}
