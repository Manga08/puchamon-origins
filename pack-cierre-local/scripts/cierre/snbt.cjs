// Small strict reader for the FTB subset: compounds, lists, strings, numeric suffixes.
exports.parse=function parse(text){
 let i=0;const ws=()=>{while(i<text.length&&/\s|,/.test(text[i]))i++;};
 function quoted(){const start=i++;while(i<text.length){if(text[i]==='\\'){i+=2;continue;}if(text[i++]==='"')return JSON.parse(text.slice(start,i));}throw Error('Unterminated string');}
 function val(){ws();const c=text[i];if(c==='"')return quoted();
  if(c==='{'){i++;const out={};ws();while(text[i]!=='}'){let key;if(text[i]==='"')key=quoted();else{const start=i;while(i<text.length&&!/[\s:]/.test(text[i]))i++;key=text.slice(start,i);}ws();if(text[i++]!==':')throw Error('Expected colon at '+i);if(Object.hasOwn(out,key))throw Error('Duplicate SNBT key '+key);out[key]=val();ws();}i++;return out;}
  if(c==='['){i++;const out=[];ws();while(text[i]!==']'){if(i>=text.length)throw Error('Unterminated array');out.push(val());ws();}i++;return out;}
  const start=i;while(i<text.length&&!/[\s,\]}]/.test(text[i]))i++;const s=text.slice(start,i);if(!s)throw Error('Expected value at '+i);if(s==='true')return true;if(s==='false')return false;if(/^-?\d+(\.\d+)?[bBsSlLfFdD]?$/.test(s))return Number(s.replace(/[bBsSlLfFdD]$/,''));return s;
 }
 const out=val();ws();if(i!==text.length)throw Error('Trailing SNBT at '+i);return out;
};
