from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
gradle=(root/'app/build.gradle').read_text()
assert 'compileSdk 36' in gradle and 'targetSdk 36' in gradle
assert "applicationId 'site.lexid.reve'" in gradle
manifest=ET.parse(root/'app/src/main/AndroidManifest.xml').getroot()
app=manifest.find('application')
assert app.get('{http://schemas.android.com/apk/res/android}usesCleartextTraffic')=='false'
print('PASS: REVE Android API36 preflight')
