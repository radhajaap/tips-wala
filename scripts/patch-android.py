#!/usr/bin/env python3
"""Run after `npx cap add android`. Adds notification permission + https deep links. Usage: patch-android.py YOUR-DOMAIN"""
import sys, re
domain = sys.argv[1]
p = "android/app/src/main/AndroidManifest.xml"
s = open(p).read()
if "POST_NOTIFICATIONS" not in s:
    s = s.replace("<application", '<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />\n    <application', 1)
if "android:autoVerify" not in s:
    f = f'''<intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="https" android:host="{domain}" />
            </intent-filter>
        </activity>'''
    s = s.replace("</activity>", f, 1)
open(p, "w").write(s); print("AndroidManifest patched")
