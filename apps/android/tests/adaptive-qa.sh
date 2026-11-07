#!/usr/bin/env bash
# RE:VE: 8 UI/accessibility scenarios using an Android emulator.
# wm size simulates viewport dimensions; it does NOT test a physical hinge.
set -euo pipefail
S="${ANDROID_SERIAL:-emulator-5554}"
OUT="${QA_OUTPUT:-adaptive-qa}"
mkdir -p "$OUT"
printf 'scenario\twidth\theight\tfontScale\tresult\n' > "$OUT/results.tsv"
adb -s "$S" shell wm density 160
reset_ui(){ adb -s "$S" shell wm size reset || :; adb -s "$S" shell wm density reset || :; adb -s "$S" shell settings put system font_scale 1.0 || :; }
trap reset_ui EXIT
for spec in \
  'phone_390|390|844|1.0' 'minimum_320|320|720|1.0' \
  'fold_outer_simulated|360|780|1.0' 'fold_inner_simulated|840|900|1.0' \
  'landscape_844|844|390|1.0' 'large_text_200_phone|390|844|2.0' \
  'large_text_200_outer|360|780|2.0' 'large_text_200_inner|840|900|2.0'; do
  IFS='|' read -r label width height scale <<< "$spec"
  echo "TEST $label: $width x $height @ $scale font"
  adb -s "$S" shell wm size "${width}x${height}"
  adb -s "$S" shell settings put system font_scale "$scale"
  adb -s "$S" shell am force-stop site.lexid.reve
  adb -s "$S" shell am start -W -n site.lexid.reve/.MainActivity -a android.intent.action.VIEW \
    -d 'https://reve.lexid.site/collection/?appCatalog=20261107d' > "$OUT/$label-start.txt"
  sleep 3
  adb -s "$S" exec-out screencap -p > "$OUT/$label.png"
  test -s "$OUT/$label.png"
  if ! timeout 25 adb -s "$S" shell uiautomator dump /sdcard/reve-qa.xml > "$OUT/$label-dump.log" 2>&1; then
    printf '%s\t%s\t%s\t%s\tBLOCKED:uiautomator\n' "$label" "$width" "$height" "$scale" >> "$OUT/results.tsv"
    exit 1
  fi
  adb -s "$S" pull /sdcard/reve-qa.xml "$OUT/$label.xml" > /dev/null
  python3 - "$OUT/$label.xml" "$label" "$width" "$height" <<'PY'
import re, sys, xml.etree.ElementTree as ET
file,scenario,width,height=sys.argv[1],sys.argv[2],int(sys.argv[3]),int(sys.argv[4])
nodes=[n for n in ET.parse(file).getroot().iter('node') if n.get('package')=='site.lexid.reve' and n.get('class')=='android.widget.Button']
for title in ('홈','제품','사운드랩','저널','공유'):
    buttons=[n for n in nodes if n.get('content-desc')==title]
    assert len(buttons)==1, (scenario,title,'missing accessibility name',[(n.get('text'),n.get('content-desc')) for n in nodes])
    b=buttons[0]
    assert b.get('enabled')=='true' and b.get('clickable')=='true', (scenario,title,'not actionable')
    coords=list(map(int,re.findall(r'\d+',b.get('bounds',''))))
    assert len(coords)==4, (scenario,title,'missing bounds')
    x1,y1,x2,y2=coords
    assert 0<=x1<x2<=width and 0<=y1<y2<=height, (scenario,title,'clipped',coords)
    assert x2-x1>=48 and y2-y1>=48, (scenario,title,'touch target under 48dp',coords)
print(scenario,': PASS native semantics and 48dp touch targets')
PY
  printf '%s\t%s\t%s\t%s\tPASS\n' "$label" "$width" "$height" "$scale" >> "$OUT/results.tsv"
done
adb -s "$S" logcat -d -v time > "$OUT/logcat.txt"
python3 - "$OUT/logcat.txt" <<'PY'
import pathlib,sys
s=pathlib.Path(sys.argv[1]).read_text(errors='replace')
assert not ('FATAL EXCEPTION' in s and 'Process: site.lexid.reve' in s), 'Possible app crash: inspect logcat'
PY
cat "$OUT/results.tsv"
