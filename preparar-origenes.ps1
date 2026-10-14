param([string]$Client,[string]$Project)
$ErrorActionPreference='Stop'
$base=$PSScriptRoot
if(-not $Project){$Project=if($env:PUCHAMON_PROJECT){$env:PUCHAMON_PROJECT}else{Split-Path $base -Parent}}
if(-not $Client){$Client=Join-Path $env:APPDATA 'FreesmLauncher\instances\Puchamon-Origins-Prueba-r3\minecraft'}
New-Item -ItemType Directory -Force -Path "$base\build","$base\cache" | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z=[IO.Compression.ZipFile]::OpenRead("$project\auditoria-cobbleverse-4SKGla61\COBBLEVERSE 1.7.42.mrpack")
try{
 $r=[IO.StreamReader]::new($z.GetEntry('modrinth.index.json').Open());try{$r.ReadToEnd() | Set-Content "$base\build\upstream-index.json" -Encoding utf8}finally{$r.Dispose()}
 $defaults="$base\build\upstream-defaults"
 New-Item -ItemType Directory -Force -Path $defaults | Out-Null
 foreach($e in $z.Entries){
  if(-not $e.FullName.StartsWith('overrides/') -or $e.FullName.EndsWith('/')){continue}
  $relative=$e.FullName.Substring(10)
  if($relative -notmatch '^(config/|resourcepacks/|shaderpacks/|fancymenu_data/|licenses/|emotes/|COBBLEVERSE - Third-Party Licenses\.pdf$)' -or $relative -match '(?i)(quickskin|account|password|\.db|sodium-fingerprint|\.log$|\.done$)'){continue}
  $dest=[IO.Path]::GetFullPath((Join-Path $defaults $relative))
  if(-not $dest.StartsWith([IO.Path]::GetFullPath($defaults)+'\',[StringComparison]::OrdinalIgnoreCase)){throw 'Ruta de archivo fuera del pack'}
  New-Item -ItemType Directory -Force -Path (Split-Path $dest -Parent) | Out-Null
  if(-not (Test-Path -LiteralPath $dest)){[IO.Compression.ZipFileExtensions]::ExtractToFile($e,$dest)}
 }
}finally{$z.Dispose()}
if(-not (Test-Path "$base\cache\modrinth-hashes.json")){
 $hashes=@(Get-ChildItem "$client\mods" -Filter '*.jar' | ForEach-Object {(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA512).Hash.ToLowerInvariant()})
 $reply=Invoke-RestMethod 'https://api.modrinth.com/v2/version_files' -Method Post -ContentType 'application/json' -Body (@{hashes=$hashes;algorithm='sha512'} | ConvertTo-Json -Depth 3) -Headers @{'User-Agent'='Puchamon-private-pack/1'}
 $reply | ConvertTo-Json -Depth 30 | Set-Content "$base\cache\modrinth-hashes.json" -Encoding utf8
}
'Origen oficial y valores iniciales preparados. No se copiaron cuentas, mundos ni preferencias de la instancia jugada.'
