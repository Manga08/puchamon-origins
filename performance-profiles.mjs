// Initial defaults only. Never apply automatically to an existing player's options.txt.
export const PROFILES = Object.freeze({
  balanced: {renderDistance:8, simulationDistance:6, graphicsMode:0, particles:1, maxFps:60, enableVsync:false, renderClouds:'"false"'},
  ligero: {renderDistance:6, simulationDistance:6, graphicsMode:0, particles:1, maxFps:60, enableVsync:false, renderClouds:'"false"'},
});
export function applyProfile(text, profile) {
  const settings=PROFILES[profile]; if(!settings) throw Error('Unknown performance profile: '+profile);
  const lines=text.replace(/^\uFEFF/,'').split(/\r?\n/).filter(Boolean);
  const seen=new Set();
  const out=lines.map(line=>{const key=line.split(':',1)[0];if(!(key in settings))return line;seen.add(key);return key+':'+settings[key];});
  for(const [key,value] of Object.entries(settings))if(!seen.has(key))out.push(key+':'+value);
  return out.join('\n')+'\n';
}
