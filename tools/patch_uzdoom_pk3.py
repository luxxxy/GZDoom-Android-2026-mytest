#!/usr/bin/env python3
"""Patch the Android-bundled UZDoom PK3 with the checked-out ZScript sources.

The Android project packages doom/src/main/assets/uzdoom.pk3 directly. Editing
wadsrc alone does not affect the APK unless the corresponding PK3 entry is
updated, so this script replaces the Doom status bar source inside that archive
while preserving every other PK3 entry.
"""
from __future__ import annotations

import os
import sys
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PK3 = ROOT / "doom/src/main/assets/uzdoom.pk3"
SOURCE = ROOT / "doom/src/main/jni/engine/gzdoom/wadsrc/static/zscript/ui/statusbar/doom_sbar.zs"
ENTRY = "zscript/ui/statusbar/doom_sbar.zs"


def main() -> int:
    if not SOURCE.is_file():
        raise SystemExit(f"Missing ZScript source: {SOURCE}")
    if not PK3.is_file():
        raise SystemExit(f"Missing bundled PK3: {PK3}")

    source_bytes = SOURCE.read_bytes()
    fd, temp_name = tempfile.mkstemp(prefix="uzdoom-", suffix=".pk3", dir=PK3.parent)
    os.close(fd)
    temp = Path(temp_name)
    try:
        with zipfile.ZipFile(PK3, "r") as original:
            infos = original.infolist()
            if original.testzip() is not None:
                raise SystemExit("Existing uzdoom.pk3 failed ZIP integrity check")
            with zipfile.ZipFile(temp, "w") as updated:
                updated.comment = original.comment
                replaced = False
                for info in infos:
                    if info.filename == ENTRY:
                        if not replaced:
                            updated.writestr(info, source_bytes)
                            replaced = True
                        continue
                    updated.writestr(info, original.read(info.filename))
                if not replaced:
                    updated.writestr(ENTRY, source_bytes, compress_type=zipfile.ZIP_DEFLATED)

        with zipfile.ZipFile(temp, "r") as check:
            if check.read(ENTRY) != source_bytes or check.testzip() is not None:
                raise SystemExit("Patched PK3 failed verification")

        os.replace(temp, PK3)
    finally:
        if temp.exists():
            temp.unlink()

    print(f"Updated and verified {ENTRY} inside {PK3}")
    print(f"Source bytes: {len(source_bytes)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
