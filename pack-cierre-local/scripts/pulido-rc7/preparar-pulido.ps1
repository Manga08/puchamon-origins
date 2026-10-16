$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$client=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'
$output=Join-Path $project 'pulido-rc7'
$assets=Join-Path $output 'generado/interfaz'
New-Item -ItemType Directory -Force -Path $assets,"$output/evidencias" | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$own=Get-Content -LiteralPath "$PSScriptRoot/inventario-es.json" -Raw | ConvertFrom-Json -AsHashtable
$coverage=@()
foreach($spec in @(@{pattern='InventoryProfilesNext*.jar';ns='inventoryprofilesnext'},@{pattern='libIPN*.jar';ns='libipn'})){
 $jar=@(Get-ChildItem -LiteralPath "$client/mods" -Filter $spec.pattern)
 if($jar.Count -ne 1){throw 'Versión de IPN ambigua'}
 $zip=[IO.Compression.ZipFile]::OpenRead($jar[0].FullName)
 try{
  $reader=[IO.StreamReader]::new($zip.GetEntry("assets/$($spec.ns)/lang/en_us.json").Open())
  try{$english=$reader.ReadToEnd() | ConvertFrom-Json -AsHashtable}finally{$reader.Dispose()}
  $spanish=[ordered]@{}
  foreach($key in $english.Keys){
   $value=$english[$key]
   if(-not $own.ContainsKey($value)){continue}
   $translation=$own[$value]
   $tokensEn=@([regex]::Matches($value,'%(?:\d+\$)?[sd]|%%|§[0-9a-fklmnor]') | ForEach-Object Value | Sort-Object)
   $tokensEs=@([regex]::Matches($translation,'%(?:\d+\$)?[sd]|%%|§[0-9a-fklmnor]') | ForEach-Object Value | Sort-Object)
   if(($tokensEn -join '|') -cne ($tokensEs -join '|')){throw "Cambió un parámetro o formato de $key"}
   if($translation.Contains([char]0xFFFD)){throw "Texto dañado en $key"}
   $spanish[$key]=$translation
  }
  if($spec.ns -eq 'libipn' -and $spanish.Count -ne $english.Count){throw 'Quedaron textos sin traducir en libIPN'}
  foreach($locale in @('es_es','es_mx','es_ar','es_cl','es_ec','es_uy','es_ve')){
   $folder="$assets/assets/$($spec.ns)/lang"; New-Item -ItemType Directory -Force -Path $folder | Out-Null
   $spanish | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath "$folder/$locale.json" -Encoding utf8NoBOM
  }
  $coverage+=@{namespace=$spec.ns;translated=$spanish.Count;sourceKeys=$english.Count;jar=$jar[0].Name;sha256=(Get-FileHash $jar[0].FullName).Hash}
 }finally{$zip.Dispose()}
}
@{schemaVersion=1;id='puchamon_ui_polish';version='1.0.0';name='Puchamon: Origins - Interfaz en español';environment='client';description='Traducción propia de la interfaz habitual de IPN y libIPN. Sin teclas ni gráficos.';license='AGPL-3.0-only';depends=@{fabricloader='>=0.19.5';minecraft='1.21.1';inventoryprofilesnext='2.2.6';libipn='6.6.3'}} | ConvertTo-Json -Depth 10 | Set-Content "$assets/fabric.mod.json" -Encoding utf8NoBOM
Copy-Item -LiteralPath "$PSScriptRoot/inventario-es.json" -Destination "$assets/FUENTE-TRADUCCION.json" -Force
'Traducciones propias de Puchamon para las claves instaladas de Inventory Profiles Next y libIPN. No reemplazan sus JAR, no copian configuraciones personales y no traducen los nombres de mods. Fuente de referencia: https://inventory-profiles-next.github.io/ y los JAR fijados en el informe de cobertura. AGPL-3.0-only.' | Set-Content "$assets/CREDITOS.txt" -Encoding utf8NoBOM
$license="$output/AGPL-3.0.txt"
if(-not (Test-Path -LiteralPath $license)){Invoke-WebRequest 'https://raw.githubusercontent.com/blackd/Inventory-Profiles/all-in-one/LICENSE' -OutFile $license}
Copy-Item -LiteralPath $license -Destination "$assets/LICENSE" -Force
$jdk=Join-Path $env:APPDATA 'FreesmLauncher/java/java-runtime-delta/bin'
& "$jdk/jar.exe" cf "$output/puchamon-ui-polish-1.0.0.jar" -C $assets .
if($LASTEXITCODE){throw 'No se empaquetó la traducción'}
$coverage | ConvertTo-Json -Depth 10 | Set-Content "$output/evidencias/cobertura-inventario.json" -Encoding utf8NoBOM
& node "$PSScriptRoot/pulir-guia.cjs"
if($LASTEXITCODE){throw 'No se preparó la guía'}
$coverage | ConvertTo-Json -Depth 10
