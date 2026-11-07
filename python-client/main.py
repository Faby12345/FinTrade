"""Run the FinTrade desktop client."""

import tkinter as tk


if tk.TkVersion < 8.6:
    raise SystemExit(
        "FinTrade requires Tk 8.6 or newer. "
        "Apple's /usr/bin/python3 uses the unsupported Tk 8.5. "
        "Run: bash python-client/run.sh"
    )

from fintrade_client.app import FinTradeApp


def main() -> None:
    FinTradeApp().run()


if __name__ == "__main__":
    main()
