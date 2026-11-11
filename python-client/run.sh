#!/usr/bin/env bash

set -euo pipefail

CLIENT_DIR="$(cd "$(dirname "$0")" && pwd)"
PYTHON_BIN="/opt/homebrew/bin/python3.14"

if [[ ! -x "$PYTHON_BIN" ]]; then
    echo "Homebrew Python 3.14 was not found at $PYTHON_BIN." >&2
    echo "Install it and its Tk binding with:" >&2
    echo "  brew install python@3.14 python-tk@3.14" >&2
    exit 1
fi

exec "$PYTHON_BIN" "$CLIENT_DIR/main.py"
