import {test} from 'node:test';
import assert from 'node:assert/strict';
import {assertExactVersion} from './version-policy.mjs';
const current={packId:'puchamon-origins',release:'1.0-auto.7',compatibility:'puchamon-1.0-auto.7'};
test('exact client and server version accepted',()=>assert.doesNotThrow(()=>assertExactVersion(current,{...current})));
test('legacy shared compatibility rejected',()=>assert.throws(()=>assertExactVersion({...current,compatibility:'puchamon-0.1.0-rc7.1-amigos'})));
test('previous server version rejected',()=>assert.throws(()=>assertExactVersion(current,{...current,release:'1.0-auto.6',compatibility:'puchamon-1.0-auto.6'})));
test('different pack rejected',()=>assert.throws(()=>assertExactVersion({...current,packId:'other'})));
