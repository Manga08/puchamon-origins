// Normal client compatibility checks, not cryptographic attestation or anti-cheat.
export function assertExactVersion(manifest, marker) {
  if (!/^[a-z0-9][a-z0-9._-]{1,99}$/.test(manifest.release || '') ||
      manifest.packId !== 'puchamon-origins' ||
      manifest.compatibility !== 'puchamon-' + manifest.release) {
    throw Error('Cada publicación debe exigir su propia versión exacta; no se permite compartir compatibilidad entre parches.');
  }
  if (marker && (marker.packId !== manifest.packId || marker.release !== manifest.release || marker.compatibility !== manifest.compatibility)) {
    throw Error('La versión exigida por el servidor no coincide con la publicación del cliente.');
  }
}
