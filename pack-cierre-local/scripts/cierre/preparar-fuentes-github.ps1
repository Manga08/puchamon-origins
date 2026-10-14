$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$repo=Join-Path $project 'distribucion/repositorio'
if(git -C $repo status --porcelain){throw 'Repositorio con cambios: no sobreescribir trabajo ajeno'}
foreach($file in Get-ChildItem "$project/distribucion" -File | Where-Object {$_.Extension -in @('.mjs','.ps1','.md','.mf') -or $_.Name -in @('LICENSE','.gitignore','.gitattributes')}){Copy-Item -LiteralPath $file.FullName -Destination "$repo/$($file.Name)" -Force}
foreach($sub in @('src','gate')){
 foreach($file in Get-ChildItem "$project/distribucion/$sub" -File -Recurse){
  $rel=[IO.Path]::GetRelativePath("$project/distribucion",$file.FullName);$dest=Join-Path $repo $rel
  New-Item -ItemType Directory -Path (Split-Path $dest -Parent) -Force | Out-Null
  Copy-Item -LiteralPath $file.FullName -Destination $dest -Force
 }
}
$pack="$repo/pack-cierre-local"
New-Item -ItemType Directory -Force -Path $pack | Out-Null
foreach($name in @('CIERRE-LOCAL-0.1.0-rc6.md','GUIA-SOCIAL-RECOMPENSAS.md','guia-social-identidades.json')){Copy-Item -LiteralPath "$project/$name" -Destination $pack}
foreach($root in @('scripts/cierre','compat/puchamon-safety','operacion')){
 foreach($file in Get-ChildItem "$project/$root" -File -Recurse | Where-Object {$_.Extension -in @('.java','.ps1','.cjs','.json','.mf','.md','.mcmeta') -and $_.FullName -notmatch '[/\\]build[/\\]'}){
  $rel=[IO.Path]::GetRelativePath($project,$file.FullName);$dest=Join-Path $pack $rel
  New-Item -ItemType Directory -Path (Split-Path $dest -Parent) -Force | Out-Null
  Copy-Item -LiteralPath $file.FullName -Destination $dest -Force
 }
}
$evidence="$repo/verificacion/rc6"
New-Item -ItemType Directory -Force -Path $evidence | Out-Null
foreach($name in @('guia-social-validacion.json','traduccion-validacion.json','conservacion-final.json','qa-runtime.json','lootr-r6-runtime.json')){Copy-Item -LiteralPath "$project/cierre-rc6/evidencias/$name" -Destination $evidence}
# Raw logs, archives, private paths/credentials, worlds, full JARs and full instances are never copied.
$bad=@(Get-ChildItem -LiteralPath $pack -Recurse -File | Where-Object {$_.Extension -in @('.jar','.class','.db','.log','.zip','.key','.pem')})
if($bad.Count){throw 'Archivo no publicable en las fuentes'}
'Fuentes cerradas y evidencias resumidas preparadas. No se subió contenido aún.'
