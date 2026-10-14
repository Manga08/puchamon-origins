$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$source=Join-Path $project 'prueba-social-r3/server'
$dest=Join-Path $project 'cierre-rc6/server'
$backup=Join-Path $project 'cierre-rc6/respaldo-privado'
$client=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'
if(Get-NetTCPConnection -State Listen -LocalPort 25577 -ErrorAction SilentlyContinue){throw 'La fuente sigue encendida'}
if(Test-Path -LiteralPath $dest){throw 'La copia ya existe; no sobreescribir'}
New-Item -ItemType Directory -Force -Path $dest,$backup | Out-Null
$rows=[Collections.Generic.List[object]]::new()
foreach($dir in @('config','datapacks','world-complementos','EasyAuth','essentialcommands','puchamon')){
 $p=Join-Path $source $dir
 if(Test-Path -LiteralPath $p){Copy-Item -LiteralPath $p -Destination (Join-Path $backup $dir) -Recurse
  foreach($f in Get-ChildItem -LiteralPath $p -Recurse -File){
   $rel=[IO.Path]::GetRelativePath($source,$f.FullName)
   $hash=(Get-FileHash -LiteralPath $f.FullName -Algorithm SHA256).Hash
   if((Get-FileHash -LiteralPath (Join-Path $backup $rel) -Algorithm SHA256).Hash -ne $hash){throw "Respaldo incorrecto: $rel"}
   $rows.Add(@{path=$rel;sha256=$hash})
  }
 }
}
foreach($f in @('server.properties','ops.json','whitelist.json','eula.txt')){if(Test-Path -LiteralPath "$source/$f"){Copy-Item -LiteralPath "$source/$f" -Destination $backup}}
@{offline=$true;verified=$true;files=$rows} | ConvertTo-Json -Depth 10 | Set-Content "$backup/verificacion.json" -Encoding utf8NoBOM
foreach($dir in @('libraries','versions','.fabric','config','datapacks','world-complementos')){
 Copy-Item -LiteralPath "$source/$dir" -Destination "$dest/$dir" -Recurse
}
New-Item -ItemType Directory "$dest/mods" | Out-Null
Get-ChildItem -LiteralPath "$source/mods" -File -Filter '*.jar' | Copy-Item -Destination "$dest/mods"
foreach($f in @('fabric-server-launch.jar','eula.txt','server.properties')){Copy-Item -LiteralPath "$source/$f" -Destination $dest}
# Never copy authentication DB, permissions DB or personal profiles into the QA server.
$props=Get-Content "$dest/server.properties" -Raw
$props=$props -replace '(?m)^server-port=.*','server-port=25579' -replace '(?m)^server-ip=.*','server-ip=127.0.0.1'
$props=$props -replace '(?m)^white-list=.*','white-list=false' -replace '(?m)^enable-rcon=.*','enable-rcon=false'
$props=$props -replace '(?m)^difficulty=.*','difficulty=normal' -replace '(?m)^gamemode=.*','gamemode=survival'
$props | Set-Content "$dest/server.properties" -Encoding utf8NoBOM
$voice="$dest/config/voicechat/voicechat-server.properties"
if(Test-Path $voice){(Get-Content $voice -Raw) -replace '(?m)^port=.*','port=24457' | Set-Content $voice -Encoding utf8NoBOM}
$protected=@(foreach($rel in @('options.txt','servers.dat','xaero','quickskin','quickskin_cache','cobblenav','saves')){
 $p=Join-Path $client $rel
 if(Test-Path -LiteralPath $p){foreach($f in Get-ChildItem -LiteralPath $p -Recurse -File){@{path=[IO.Path]::GetRelativePath($client,$f.FullName);sha256=(Get-FileHash -LiteralPath $f.FullName -Algorithm SHA256).Hash}}}
})
$protected | ConvertTo-Json -Depth 8 | Set-Content "$project/cierre-rc6/evidencias/cliente-protegido-antes.json" -Encoding utf8NoBOM
"Copia aislada preparada; respaldo offline verificado: $($rows.Count) archivos. Datos privados fuera de distribución."
